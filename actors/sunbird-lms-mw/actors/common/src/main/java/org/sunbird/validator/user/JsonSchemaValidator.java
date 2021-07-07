package org.sunbird.validator.user;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.sunbird.common.models.util.LoggerUtil;
import org.sunbird.common.models.util.ProjectUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class JsonSchemaValidator {

    private static LoggerUtil logger = new LoggerUtil(UserRequestValidator.class);

    private static Map<String, String> schemas = new HashMap<>();
    private static final String SCHEMA_PATH = ProjectUtil.getConfigValue("schema_path");

    public static void loadSchemas(){
        File[] files = new File(SCHEMA_PATH).listFiles();

        for (File file : files) {
            logger.info(null, String.format("file :- "+file.getName()));

            if (file.isFile()) {
                schemas.put(file.getName(), read(SCHEMA_PATH + file.getName()));

            }
        }
        logger.info(null, String.format("schemas size :- "+schemas.size()));
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

            SchemaLoader schemaLoader = SchemaLoader.builder()
                    .schemaJson(rawSchema)
                    .draftV7Support()
                    .resolutionScope("file://"+SCHEMA_PATH).build();
            schema = schemaLoader.load().build();
        } catch (Exception ioe) {
            ioe.printStackTrace();
            throw new Exception("can't validate, "+ entityType + ": schema has a problem!");
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
