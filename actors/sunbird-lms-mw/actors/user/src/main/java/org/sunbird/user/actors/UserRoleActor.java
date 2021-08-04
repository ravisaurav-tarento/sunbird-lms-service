package org.sunbird.user.actors;

import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.actor.router.ActorConfig;
import org.sunbird.cassandra.CassandraOperation;
import org.sunbird.common.exception.ProjectCommonException;
import org.sunbird.common.factory.EsClientFactory;
import org.sunbird.common.inf.ElasticSearchService;
import org.sunbird.common.models.response.Response;
import org.sunbird.common.models.util.*;
import org.sunbird.common.request.Request;
import org.sunbird.common.request.RequestContext;
import org.sunbird.common.responsecode.ResponseCode;
import org.sunbird.helper.ServiceFactory;
import org.sunbird.learner.actors.role.service.RoleService;
import org.sunbird.learner.organisation.service.OrgService;
import org.sunbird.learner.organisation.service.impl.OrgServiceImpl;
import org.sunbird.learner.util.DataCacheHandler;
import org.sunbird.learner.util.Util;
import org.sunbird.user.service.UserRoleService;
import org.sunbird.user.service.impl.UserRoleServiceImpl;

@ActorConfig(
  tasks = {"getRoles", "assignRoles"},
  asyncTasks = {},
  dispatcher = "most-used-two-dispatcher"
)
public class UserRoleActor extends UserBaseActor {

  private CassandraOperation cassandraOperation = ServiceFactory.getInstance();
  private ElasticSearchService esService = EsClientFactory.getInstance(JsonKey.REST);
  private OrgService orgService = OrgServiceImpl.getInstance();

  @Override
  public void onReceive(Request request) throws Throwable {
    Util.initializeContext(request, TelemetryEnvKey.USER);
    String operation = request.getOperation();

    switch (operation) {
      case "getRoles":
        getRoles();
        break;

      case "assignRoles":
        assignRoles(request);
        break;

      default:
        onReceiveUnsupportedOperation("UserRoleActor");
    }
  }

  private void getRoles() {
    logger.info("UserRoleActor: getRoles called");
    Response response = DataCacheHandler.getRoleResponse();
    if (response == null) {
      response = RoleService.getUserRoles();
      DataCacheHandler.setRoleResponse(response);
    }
    sender().tell(response, self());
  }

  @SuppressWarnings("unchecked")
  private void assignRoles(Request actorMessage) {
    logger.info(actorMessage.getRequestContext(), "UserRoleActor: assignRoles called");
    Response response = new Response();
    Map<String, Object> requestMap = actorMessage.getRequest();
    requestMap.put(JsonKey.REQUESTED_BY, actorMessage.getContext().get(JsonKey.USER_ID));
    requestMap.put(JsonKey.ROLE_OPERATION, "assignRole");
    List<String> roles = (List<String>) requestMap.get(JsonKey.ROLES);
    RoleService.validateRoles(roles);
    UserRoleService userRoleService = UserRoleServiceImpl.getInstance();

    String configValue = PropertiesCache.getInstance().getProperty(JsonKey.DISABLE_MULTIPLE_ORG_ROLE);
    if(Boolean.parseBoolean(configValue)) {
      validateRequest(userRoleService.readUserRole((String) requestMap.get(JsonKey.USER_ID), actorMessage.getRequestContext()),
              (String) requestMap.get(JsonKey.ORGANISATION_ID), actorMessage.getRequestContext());
    }

    List<Map<String, Object>> userRolesList =
        userRoleService.updateUserRole(requestMap, actorMessage.getRequestContext());
    if (!userRolesList.isEmpty()) {
      response.put(JsonKey.RESPONSE, JsonKey.SUCCESS);
    }
    sender().tell(response, self());
    userRolesList = userRoleService.readUserRole((String) requestMap.get(JsonKey.USER_ID), actorMessage.getRequestContext());
    ObjectMapper mapper = new ObjectMapper();
    userRolesList
            .stream()
            .forEach(
                    userRole -> {
                      try {
                        String dbScope = (String) userRole.get(JsonKey.SCOPE);
                        if (StringUtils.isNotBlank(dbScope)) {
                          List<Map<String, String>> scope = mapper.readValue(dbScope, ArrayList.class);
                          userRole.put(JsonKey.SCOPE, scope);
                        }
                      } catch (Exception e) {
                        logger.error(
                                actorMessage.getRequestContext(),
                                "Exception because of mapper read value" + userRole.get(JsonKey.SCOPE),
                                e);
                      }
                    });
    if (((String) response.get(JsonKey.RESPONSE)).equalsIgnoreCase(JsonKey.SUCCESS)) {
      syncUserRoles(
          JsonKey.USER,
          (String) requestMap.get(JsonKey.USER_ID),
          userRolesList,
          actorMessage.getRequestContext());
    } else {
      logger.info(actorMessage.getRequestContext(), "UserRoleActor: No ES call to save user roles");
      throw new ProjectCommonException(
          ResponseCode.roleSaveError.getErrorCode(),
          ResponseCode.roleSaveError.getErrorMessage(),
          ResponseCode.SERVER_ERROR.getResponseCode());
    }
    generateTelemetryEvent(
        requestMap,
        (String) requestMap.get(JsonKey.USER_ID),
        "userLevel",
        actorMessage.getContext());
  }

  private void syncUserRoles(
      String type, String userId, List<Map<String, Object>> userRolesList, RequestContext context) {
    Request request = new Request();
    request.setRequestContext(context);
    request.setOperation(ActorOperations.UPDATE_USER_ROLES_ES.getValue());
    request.getRequest().put(JsonKey.TYPE, type);
    request.getRequest().put(JsonKey.USER_ID, userId);
    request.getRequest().put(JsonKey.ROLES, userRolesList);
    logger.info(context, "UserRoleActor:syncUserRoles: Syncing to ES");
    try {
      tellToAnother(request);
    } catch (Exception ex) {
      logger.error(
          context,
          "UserRoleActor:syncUserRoles: Exception occurred with error message = " + ex.getMessage(),
          ex);
    }
  }

  private void validateRequest(List<Map<String, Object>> userRolesList, String organisationId, RequestContext context) {
    ObjectMapper mapper = new ObjectMapper();
    userRolesList
            .stream()
            .forEach(
                userRole -> {
                  try {
                    String dbScope = (String) userRole.get(JsonKey.SCOPE);
                    if (StringUtils.isNotBlank(dbScope)) {
                      List<Map<String, String>> scope = mapper.readValue(dbScope, ArrayList.class);
                      userRole.put(JsonKey.SCOPE, scope);
                      for(Map<String, String> orgScope : scope) {
                        String oldOrgId = orgScope.get("organisationId");
                        if(StringUtils.isNotBlank(oldOrgId) && !oldOrgId.equalsIgnoreCase(organisationId)) {
                          logger.info(context, "UserRoleActor: Given OrganisationId is different than existing one.");
                          throw new ProjectCommonException(
                                  ResponseCode.roleProcessingInvalidOrgError.getErrorCode(),
                                  ResponseCode.roleProcessingInvalidOrgError.getErrorMessage(),
                                  ResponseCode.SERVER_ERROR.getResponseCode());
                        }
                      }
                    }
                  } catch(ProjectCommonException pce) {
                    throw pce;
                  } catch (Exception e) {
                    logger.error(
                            context,
                            "Exception because of mapper read value" + userRole.get(JsonKey.SCOPE),
                            e);
                  }
                });
  }
}
