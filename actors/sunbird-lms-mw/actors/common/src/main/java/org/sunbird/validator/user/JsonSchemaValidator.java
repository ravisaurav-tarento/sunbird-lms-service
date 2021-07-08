package org.sunbird.validator.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.sunbird.common.exception.ProjectCommonException;
import org.sunbird.common.models.util.JsonKey;
import org.sunbird.common.models.util.LoggerUtil;
import org.sunbird.common.models.util.ProjectUtil;
import org.sunbird.learner.util.DataCacheHandler;
import org.sunbird.user.profile.ProfileUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import static org.sunbird.common.request.orgvalidator.BaseOrgRequestValidator.ERROR_CODE;

public class JsonSchemaValidator {

    private static LoggerUtil logger = new LoggerUtil(UserRequestValidator.class);
    private static Map<String, String> schemas = new HashMap<>();

    public static void loadSchemas() {

        try {
            Map<String, String> configSettingMap = DataCacheHandler.getConfigSettings();
            String schemaConfig = configSettingMap.get(JsonKey.EXTENDED_PROFILE_SCHEMA_CONFIG);
            for (Map.Entry entry : ProfileUtil.toMap(schemaConfig).entrySet()) {
                schemas.put(entry.getKey().toString(), ProfileUtil.mapper.writeValueAsString(entry.getValue()));
            }

        } catch (Exception e) {
            throw new ProjectCommonException(
                    "SCHEMA_CANNOT_LOADED",
                    e.getMessage(),
                    ERROR_CODE);
        }
        logger.info(null, String.format("schemas size :- " + schemas.size()));
    }

    public static boolean validate(String entityType, String payload) throws Exception {
        boolean result = false;
        Schema schema = getEntitySchema(entityType);
        JSONObject obj = new JSONObject(payload);
        try {
            schema.validate(obj);
            result = true;
        } catch (ValidationException e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }
        return result;
    }

    private static Schema getEntitySchema(String entityType) throws Exception {

        Schema schema;
        try {
            String definitionContent = schemas.get(entityType);
            JSONObject rawSchema = new JSONObject(definitionContent);

            SchemaLoader schemaLoader = SchemaLoader.builder().useDefaults(true)
                    .schemaJson(rawSchema).build();
            schema = schemaLoader.load().build();
        } catch (Exception ioe) {
            ioe.printStackTrace();
            throw new Exception("can't validate, " + entityType + ": schema has a problem!");
        }
        return schema;
    }

    private static String read(String file) {
        StringBuilder fileData = new StringBuilder();

        try {
            File fileObj = new File(file);
            Scanner reader = new Scanner(fileObj);
            while (reader.hasNextLine()) {
                fileData.append(reader.nextLine());
            }
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return fileData.toString();

    }

}
