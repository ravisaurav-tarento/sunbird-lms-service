package org.sunbird.user.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ProfileUtil {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static JsonNode toJson(String jsonStr) throws JsonProcessingException {
        return mapper.readValue(jsonStr, JsonNode.class);
    }


}
