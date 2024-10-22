package writer;

import config.HttpConfigurationAndResources;
import http1.HttpMethod;
import http1.HttpRequest;
import redis.RedisService;
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
    static final String CRLF = "\r\n";

    public static void write(OutputStream outputStream, HttpRequest request, HttpConfigurationAndResources configurationAndResources) {
        try {
            HttpMethod method = request.getMethod();
            String requestTarget = request.getRequestTarget().substring(1);
            if (!requestTarget.equals("favicon.ico")) {
                byte[] data = null;
                String fileExtension = null;

                /// Cần refactor lại code.
                // Kiểm tra requestTarget được truy cập nhiều hay không?
                RedisService redisService = new RedisService();
                int numbersRequestToTarget = redisService.getNumberRequest(requestTarget);
                if (numbersRequestToTarget >= 2) {
                    data = redisService.getBytesValue(requestTarget);
                    fileExtension = ResponseUtil.getFileExtension(configurationAndResources.getTargetResources().getResources().get(requestTarget));
                } else {
                    ObjectResponse objectResponse = ResponseUtil.readFile(requestTarget, configurationAndResources.getTargetResources());
                    fileExtension = objectResponse.getFileExtension();
                    data = objectResponse.getByteArrayOutputStream().toByteArray();
                }

                // Parallel
                byte[] finalData = data;
                Thread thead = new Thread(() -> {
                    if (numbersRequestToTarget == 0) {
                        redisService.setBytesValue(requestTarget, finalData);
                        System.out.println("Thanh cong");
                    }
                    redisService.increaseValue("number:" + requestTarget);
                    System.out.println("Lưu " + requestTarget);
                });
                thead.start();

                String contentType = null;
                if (method.name().equals(HttpMethod.GET.name())) {
                    if (fileExtension.equals("txt")) {
                        contentType = "Content-Type: text/plain; charset=UTF-8";
                    } else if (fileExtension.equals("png")) {
                        contentType = "Content-Type: image/png";
                    } else if (fileExtension.equals("jpg") || fileExtension.equals("jpeg")) {
                        contentType = "Content-Type: image/jpeg";
                    } else if (fileExtension.equals("gif")) {
                        contentType = "Content-Type: image/gif";
                    } else if (fileExtension.equals("css")) {
                        contentType = "Content-Type: text/css";
                    }

                } else if (method.name().equals(HttpMethod.POST.name())) {
                    String filepath = "simplehttpserver/src/main/resources/target_resources/uploads/" + request.getFilename();
                    BufferedOutputStream fileOutputStream = new BufferedOutputStream(new FileOutputStream(filepath));
                    fileOutputStream.write(request.getData());

                    if (!Json.writeToJsonFile(request.getFilename(), filepath)) {
                        logger.info("Can not write " + filepath + " to json file!. Cause filename already exits.");
                    }
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
}