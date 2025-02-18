package utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import config.HttpConfigurationAndResources;
import config.TargetResources;

import java.io.File;
import java.util.HashMap;

public class Json {
    private static ObjectMapper myObjectMapper = defaultObjectMapper();

    private static ObjectMapper defaultObjectMapper() {
        ObjectMapper om = new ObjectMapper();
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return om;
    }

    public static JsonNode parse(String jsonSrc) throws JsonProcessingException {
        return myObjectMapper.readTree(jsonSrc);
    }

    public static <A> A fromJson(JsonNode node, Class<A> clazz) throws JsonProcessingException {
        return myObjectMapper.treeToValue(node, clazz);
    }

    public static JsonNode toJson(Object obj) {
        return myObjectMapper.valueToTree(obj);
    }

    public static String stringify(JsonNode node) throws JsonProcessingException {
        return generateJson(node, false);
    }

    public static String stringifyPretty(JsonNode node) throws JsonProcessingException {
        return generateJson(node, true);
    }

    private static String generateJson(Object o, boolean pretty) throws JsonProcessingException {
        ObjectWriter objectWriter = myObjectMapper.writer();

        if (pretty) {
            objectWriter = objectWriter.with(SerializationFeature.INDENT_OUTPUT);
        }
        return objectWriter.writeValueAsString(o);
    }

    public static boolean writeToJsonFile(String filename, String filePath) {
        HashMap<String, String> targetHash;
        String jsonFilePath = "simplehttpserver/src/main/resources/request_target.json";
        ObjectMapper objectMapper = new ObjectMapper();
        File file = new File(jsonFilePath);

        try {
            targetHash = objectMapper.readValue(file, new TypeReference<>() {
            });

            int indexOfDot = filename.lastIndexOf('.');
            String target = filename.substring(0, indexOfDot).toLowerCase().replaceAll("\\s+", "-");
            if (targetHash.containsKey(target))
                return false;
            targetHash.put(target, filePath);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, targetHash);

            // Update targetResources instance.
            TargetResources targetResources = HttpConfigurationAndResources.getInstance().getTargetResources();
            targetResources.getResources().put(target, filePath);
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
