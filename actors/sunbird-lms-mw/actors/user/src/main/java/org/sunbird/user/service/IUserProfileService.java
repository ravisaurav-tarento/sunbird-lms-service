package org.sunbird.user.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.sunbird.common.request.Request;

public interface IUserProfileService {

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
    public boolean updateWorkflow(Request userRequest);

}
