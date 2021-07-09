package org.sunbird.user.profile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.sunbird.common.models.util.JsonKey;
import org.sunbird.common.models.util.LoggerUtil;

import java.util.*;

public class ProfileUtil {

    public static final ObjectMapper mapper = new ObjectMapper();
    private static LoggerUtil logger = new LoggerUtil(ProfileUtil.class);


    public static Map<String,Object> toMap(String jsonString) {
        try {
            TypeReference<HashMap<String, Object>> typeRef
                    = new TypeReference<HashMap<String, Object>>() {};
            Map<String, Object> map = mapper.readValue(jsonString, typeRef);
            return map;

        } catch (Exception e) {
            logger.error( "ProfileUtil Exception " , e);

        }
        return null;
    }


    public static void appendIdToRefenceObjects(Map<String, Object> profile) {

        for (Map.Entry<String, Object> entry : profile.entrySet()) {
            if (entry.getValue() instanceof ArrayList) {
                if (((ArrayList) entry.getValue()).get(0) instanceof HashMap) {
                    List<Map<String, Object>> list = (List<Map<String, Object>>) entry.getValue();
                    for (Map object : list) {
                        object.put(JsonKey.OSID, UUID.randomUUID().toString());
                    }
                }
            }
        }
    }


}
