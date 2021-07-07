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

}
