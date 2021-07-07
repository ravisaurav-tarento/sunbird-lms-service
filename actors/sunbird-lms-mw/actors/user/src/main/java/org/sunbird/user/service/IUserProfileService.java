package org.sunbird.user.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.sunbird.common.request.Request;

import java.util.Map;

public interface IUserProfileService {

    String WORKFLOW = "workflow";
    String FIELDKEY = "fieldKey";
    String TOVALUE = "toValue";

    /**
     * Validate json payload of user profile of a given user request
     * @param userRequest
     */
    public void validateProfile(Request userRequest);

    /**
     *
     * @param userRequest
     * @return
     */
    public void updateWorkflow(Map<String, Object> userRequest, Map userRecord);

}
