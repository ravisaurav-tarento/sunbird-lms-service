package org.sunbird.user.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.sunbird.common.exception.ProjectCommonException;
import org.sunbird.common.models.util.JsonKey;
import org.sunbird.common.models.util.LoggerUtil;
import org.sunbird.common.models.util.ProjectUtil;
import org.sunbird.common.request.Request;
import org.sunbird.user.profile.ProfileUtil;
import org.sunbird.user.service.IUserProfileService;
import org.sunbird.user.service.UserProfileReadService;
import org.sunbird.validator.user.JsonSchemaValidator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.sunbird.common.request.orgvalidator.BaseOrgRequestValidator.ERROR_CODE;

public class UserProfileService implements IUserProfileService {

    private LoggerUtil logger = new LoggerUtil(UserProfileReadService.class);
    private static final String SCHEMA = ProjectUtil.getConfigValue("schema");
    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        JsonSchemaValidator.loadSchemas();
    }

    @Override
    public void validateProfile(Request userRequest) {

        if (userRequest!=null && userRequest.get(JsonKey.PROFILE_DETAILS)!=null) {
            try{
                String userProfile = mapper.writeValueAsString(userRequest.getRequest().get(JsonKey.PROFILE_DETAILS));
                JsonSchemaValidator.validate(SCHEMA, userProfile);

            } catch (Exception e){
                logger.error("validate profile exception:",e);
                throw new ProjectCommonException(
                        "INVALID_PAYLOAD",
                        e.getMessage(),
                        ERROR_CODE);
            }
        }
    }

    @Override
    public void updateWorkflow(Map<String, Object> userRequest, Map userRecord){
        Map<String, Object> existingUserProfile = (Map<String, Object>) userRecord.get(JsonKey.PROFILE_DETAILS);
        logger.info("workflow existing profile details: "+existingUserProfile);
        if(userRequest.get(WORKFLOW)!=null){
            try{
                List<Map<String, Object>> requests = (List<Map<String, Object>>)userRequest.get(WORKFLOW);


                for (Map<String, Object> request : requests) {
                    String osid = null;
                    Map<String, Object> toChange = new HashMap<>();
                    Object profileObject = existingUserProfile.get(request.get(FIELDKEY));
                    Map<String, Object> searchFields = null;

                    if (profileObject instanceof ArrayList) {
                        osid = StringUtils.isEmpty(request.get(ProfileUtil.OSID).toString()) == true ? "" : request.get(ProfileUtil.OSID).toString();
                        for (Map<String, Object> obj : ((List<Map<String, Object>>) profileObject)) {
                            if (obj.get("osid").toString().equalsIgnoreCase(osid))
                                searchFields = obj;
                        }
                    }
                    if (profileObject instanceof HashMap) {
                        searchFields = (Map<String, Object>) existingUserProfile.get((String) request.get(FIELDKEY));
                    }
                    toChange.putAll(searchFields);

                    Map<String, Object> objectMap = (Map<String, Object>) request.get(TOVALUE);
                    for (Map.Entry entry : objectMap.entrySet())
                        toChange.put((String) entry.getKey(), entry.getValue());
                    ProfileUtil.mergeLeaf(existingUserProfile, toChange, request.get(FIELDKEY).toString(),osid );
                }
                userRequest.put(JsonKey.PROFILE_DETAILS, existingUserProfile);

            }catch (Exception e){
                logger.error("Workflow Update exception:",e);

            }
        }
    }




}
