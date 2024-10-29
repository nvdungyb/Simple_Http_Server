package writer;

import config.HttpConfigurationAndResources;
import http1.HttpMethod;
import http1.HttpRequest;
import redis.RedisService;
import redis.clients.jedis.Jedis;
import util.Json;
import util.ObjectResponse;
import util.ResponseUtil;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;

public class ResponseWriter {
    private static Logger logger = Logger.getLogger(ResponseWriter.class.getName());
    private static final int NUMBER_REQUEST_TO_CACHE = 2;
    static final String CRLF = "\r\n";

    public static void write(OutputStream outputStream, HttpRequest request, HttpConfigurationAndResources configurationAndResources) {
        try {
            HttpMethod method = request.getMethod();
            String requestTarget = request.getRequestTarget().substring(1);
            if (!requestTarget.equals("favicon.ico")) {
                byte[] data = null;
                String fileExtension = null;

                /// Need to refactor this code
                // Get data from redis server if it is cached.
                int numbersRequestToTarget = 0;
                boolean isRedisConnected = true;
                try {
                    numbersRequestToTarget = RedisService.getNumberRequest(requestTarget);
                } catch (
                        Exception e) {                                           // In case: can not connect to redis server.
                    isRedisConnected = false;
                }
                if (numbersRequestToTarget > NUMBER_REQUEST_TO_CACHE) {                                   // This resource is cached.
                    data = RedisService.getBytesValue(requestTarget);
                    fileExtension = ResponseUtil.getFileExtension(configurationAndResources.getTargetResources().getResources().get(requestTarget));
                } else {
                    // Need to check if Server contain this file or not.
                    ObjectResponse objectResponse = ResponseUtil.readFile(requestTarget, configurationAndResources.getTargetResources());
                    fileExtension = objectResponse.getFileExtension();
                    data = objectResponse.getByteArrayOutputStream().toByteArray();
                }

                if (isRedisConnected) {
                    cacheResource(data, numbersRequestToTarget, requestTarget);
                }

                String contentType = null;
                if (method.name().equals(HttpMethod.GET.name())) {
                    contentType = getContentType(fileExtension);
                } else if (method.name().equals(HttpMethod.POST.name())) {
                    uploadResource(request);
                }

                if (contentType == null) contentType = "Content-Type: text/html";

                String responseHeader = "HTTP/1.1 200 OK" + CRLF + contentType + CRLF + "Content-Length: " + data.length + CRLF + CRLF;
                outputStream.write(responseHeader.getBytes());
                outputStream.write(data);

            } else {
                logger.info("/favicon.ico doesn't excepted.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void uploadResource(HttpRequest request) throws IOException {
        String filepath = "simplehttpserver/src/main/resources/target_resources/uploads/" + request.getFilename();
        BufferedOutputStream fileOutputStream = new BufferedOutputStream(new FileOutputStream(filepath));
        fileOutputStream.write(request.getData());

        if (!Json.writeToJsonFile(request.getFilename(), filepath)) {
            logger.info("Can not write " + filepath + " to json file!. Cause filename already exits.");
        }
    }

    private static String getContentType(String fileExtension) {
        if (fileExtension.equals("txt")) {
            return "Content-Type: text/plain; charset=UTF-8";
        } else if (fileExtension.equals("png")) {
            return "Content-Type: image/png";
        } else if (fileExtension.equals("jpg") || fileExtension.equals("jpeg")) {
            return "Content-Type: image/jpeg";
        } else if (fileExtension.equals("gif")) {
            return "Content-Type: image/gif";
        } else if (fileExtension.equals("css")) {
            return "Content-Type: text/css";
        }
        return null;
    }

    private static void cacheResource(byte[] data, int numbersRequestToTarget, String requestTarget) {
        // parallel cache resource to reduce response time.
        byte[] finalData = data;
        Jedis jedis = RedisService.getConnection();
        Thread thead = new Thread(() -> {
            if (numbersRequestToTarget == NUMBER_REQUEST_TO_CACHE) {
                RedisService.setBytesValue(requestTarget, finalData);
                System.out.println("Cached resource successfully");
            } else if (numbersRequestToTarget > NUMBER_REQUEST_TO_CACHE) {
                RedisService.setExpireKey(jedis, requestTarget);
            }
            RedisService.increaseValue(requestTarget);
            System.out.println("Saved: " + requestTarget);
        });
        thead.start();
    }
}