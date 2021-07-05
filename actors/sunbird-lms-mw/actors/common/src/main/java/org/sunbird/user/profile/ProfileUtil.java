package org.sunbird.user.profile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

public class ProfileUtil {

    private static final String OSID = "osid";
    private static final ObjectMapper mapper = new ObjectMapper();

    public static Map toMap(String jsonString) {
        try {
            TypeReference<HashMap<String, Object>> typeRef
                    = new TypeReference<HashMap<String, Object>>() {
            };
            Map<String, Object> map = new ObjectMapper().readValue(jsonString, typeRef);
            return map;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static void appendIdToRefenceObjects(Map<String, Object> profile) {

        for (Map.Entry<String, Object> entry : profile.entrySet()) {
            if (entry.getValue() instanceof ArrayList) {
                if (((ArrayList) entry.getValue()).get(0) instanceof HashMap) {
                    List<Map<String, Object>> list = (List<Map<String, Object>>) entry.getValue();
                    for (Map object : list) {
                        object.put(OSID, UUID.randomUUID().toString());
                    }
                }
            }
        }
    }


}
