package org.sunbird.user.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.exception.ProjectCommonException;
import org.sunbird.common.request.Request;
import org.sunbird.user.service.IUserProfileService;
import org.sunbird.validator.user.JsonSchemaValidator;

import java.util.List;
import java.util.Map;

import static org.sunbird.common.request.orgvalidator.BaseOrgRequestValidator.ERROR_CODE;

public class UserProfileService implements IUserProfileService {

    private static final String SCHEMA = "profileDetails.json";
    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        JsonSchemaValidator.loadSchemas();
    }

    @Override
    public void validateProfile(Request userRequest) {

        if (userRequest!=null) {
            try{
                String userProfile = mapper.writeValueAsString(userRequest.getRequest().get("profileDetails"));
                JsonSchemaValidator.validate(SCHEMA, userProfile);

            } catch (Exception e){
                e.printStackTrace();
                throw new ProjectCommonException(
                        "INVALID_PAYLOAD",
                        e.getMessage(),
                        ERROR_CODE);
            }
        }
    }

    @Override
    public boolean updateWorkflow(Request userRequest){
        return true;
    }



}
