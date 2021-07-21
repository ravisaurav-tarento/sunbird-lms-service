package org.sunbird.user.service.impl;

import org.json.JSONObject;
import org.sunbird.common.exception.ProjectCommonException;
import org.sunbird.common.models.util.JsonKey;
import org.sunbird.common.models.util.LoggerUtil;
import org.sunbird.common.request.Request;
import org.sunbird.user.profile.ProfileUtil;
import org.sunbird.user.service.IUserProfileService;
import org.sunbird.user.service.UserProfileReadService;
import org.sunbird.validator.user.JsonSchemaValidator;


import java.util.Map;

import static org.sunbird.common.request.orgvalidator.BaseOrgRequestValidator.ERROR_CODE;

public class UserProfileService implements IUserProfileService {

    private LoggerUtil logger = new LoggerUtil(UserProfileReadService.class);
    private static final String SCHEMA = "profileDetails.json";



    @Override
    public void validateProfile(Request userRequest) {

        if (userRequest!=null && userRequest.getRequest().get(JsonKey.PROFILE_DETAILS)!=null) {
            try{
                JsonSchemaValidator.loadSchemas();
                String userProfile = ProfileUtil.mapper.writeValueAsString(userRequest.getRequest().get(JsonKey.PROFILE_DETAILS));
                JSONObject obj = new JSONObject(userProfile);
                JsonSchemaValidator.validate(SCHEMA, obj);
                ((Map)userRequest.getRequest().get(JsonKey.PROFILE_DETAILS)).put(JsonKey.MANDATORY_FIELDS_EXISTS, obj.get(JsonKey.MANDATORY_FIELDS_EXISTS));

            } catch (Exception e){
                logger.error("validate profile exception:",e);
                throw new ProjectCommonException(
                        "INVALID_PAYLOAD",
                        e.getMessage(),
                        ERROR_CODE);
            }
        }
    }

}
