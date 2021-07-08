package org.sunbird.user.profile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

public class ProfileUtil {

    public static final String OSID = "osid";
    public static final ObjectMapper mapper = new ObjectMapper();

    public static Map<String,Object> toMap(String jsonString) {
        try {
            TypeReference<HashMap<String, Object>> typeRef
                    = new TypeReference<HashMap<String, Object>>() {};
            Map<String, Object> map = mapper.readValue(jsonString, typeRef);
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


    public static void mergeLeaf(Map<String,Object> mapLeft, Map<String,Object> mapRight, String leafKey , String id) {
        // go over all the keys of the right map

        for (String key : mapLeft.keySet()) {

            if(key.equalsIgnoreCase(leafKey) && (mapLeft.get(key) instanceof ArrayList)){

                ((ArrayList)mapLeft.get(key)).removeIf(o -> ((Map)o).get(OSID).toString().equalsIgnoreCase(id));
                ((ArrayList)mapLeft.get(key)).add(mapRight);

            }
            if(key.equalsIgnoreCase(leafKey) && (mapLeft.get(key) instanceof HashMap)){
                mapLeft.put(key, mapRight);

            }

        }
    }



}
