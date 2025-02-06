import config.ConfigurationManager;
import config.HttpConfiguration;
import config.HttpConfigurationAndResources;
import config.TargetResources;
import core.ServerListenerThread;

import java.util.HashMap;
import java.util.logging.Logger;

/**
 * Driver class for Http server.
 */
public class HttpServer {
    private static final Logger logger = Logger.getLogger(HttpServer.class.getName());

    public static void main(String[] args) {
        logger.info("Server starting...");

        HttpConfiguration httpConfiguration = ConfigurationManager.getInstance().loadConfigurationFile("src/main/resources/http.json", HttpConfiguration.class);
        HashMap<String, String> resources = ConfigurationManager.getInstance().loadConfigurationFile("src/main/resources/request_target.json", HashMap.class);
        TargetResources targetResources = new TargetResources(resources);

        HttpConfigurationAndResources httpConfigurationAndResources = HttpConfigurationAndResources.getInstance();
        httpConfigurationAndResources.setHttpConfiguration(httpConfiguration);
        httpConfigurationAndResources.setTargetResources(targetResources);

        logger.info(httpConfigurationAndResources.toString());

        try {
            ServerListenerThread serverListenerThread = new ServerListenerThread(httpConfiguration.getPort(), httpConfiguration.getWebroot());
            serverListenerThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
