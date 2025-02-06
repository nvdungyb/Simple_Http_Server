package config;

import com.fasterxml.jackson.databind.ObjectMapper;
import exception.HttpConfigurationException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class ConfigurationManager {
    private static ConfigurationManager myConfigurationManager;

    private ConfigurationManager() {
    }

    public static ConfigurationManager getInstance() {
        if (myConfigurationManager == null)
            myConfigurationManager = new ConfigurationManager();
        return myConfigurationManager;
    }

    /**
     * Used to load a configuration by the path provided.
     *
     * @param filePath
     */
    public <T> T loadConfigurationFile(String filePath, Class<T> clazz) {
        try (FileReader reader = new FileReader(filePath)) {
            StringBuilder sb = new StringBuilder();
            int i;
            while ((i = reader.read()) != -1) {
                sb.append((char) i);
            }

            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(sb.toString(), clazz);
        } catch (FileNotFoundException ex) {
            throw new HttpConfigurationException(ex);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
