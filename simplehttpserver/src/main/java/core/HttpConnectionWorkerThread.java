package core;

import analysis_request.StatisticResponse;
import config.HttpConfigurationAndResources;
import http1.HttpMethod;
import http1.HttpParser;
import http1.HttpParsingException;
import http1.HttpRequest;
import proxy.BlackList;
import redis.RedisService;
import writer.ResponseWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Map;
import java.util.logging.Logger;

public class HttpConnectionWorkerThread extends Thread {
    private Socket socket;
    private HttpConfigurationAndResources configurationAndResources;
    private final static Logger logger = Logger.getLogger(HttpConnectionWorkerThread.class.getName());

    public HttpConnectionWorkerThread(Socket socket) {
        this.socket = socket;
        this.configurationAndResources = HttpConfigurationAndResources.getInstance();
    }

    @Override
    public void run() {
        InputStream clientInputStream = null;
        OutputStream clientOutputStream = null;
        try {
            clientInputStream = socket.getInputStream();
            clientOutputStream = socket.getOutputStream();

            HttpRequest request = new HttpParser().parseHttpRequest(clientInputStream);

            if (isBlacklisted(request, BlackList.getInstance())) {
                ResponseWriter.sendAccessDeniedResponse(request, clientOutputStream);
            } else {

                statisticRequest(request);

                if (request.getHeader().get("Host").startsWith("localhost")) {
                    if (request.getRequestTarget().equals("/analysis")) {
                        StatisticResponse.analysisResponse(clientOutputStream);
                    } else {
                        ResponseWriter.write(clientOutputStream, request, configurationAndResources);
                    }
                } else {
                    if (HttpMethod.CONNECT.name().equalsIgnoreCase(request.getMethod().name())) {
                        handleConnectRequest(request, clientOutputStream, clientInputStream);
                    } else {
                        handleHttpRequest(request, clientOutputStream);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (HttpParsingException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                clientInputStream.close();
            } catch (IOException e) {
            }
            try {
                clientOutputStream.close();
            } catch (IOException e) {
            }
            if (socket != null) {
                try {
                    socket.close();
                    logger.info("Connection was closed!");
                } catch (IOException e) {
                }
            }
        }
    }

    private boolean isBlacklisted(HttpRequest request, BlackList instance) {
        String host = request.getHeader().get("Host");
        String target = request.getRequestTarget();
        return instance.blacklist.containsKey(host) && instance.blacklist.get(host).contains(target);
    }

    private void statisticRequest(HttpRequest request) {
        String host = request.getHeader().get("Host");
        String target = request.getRequestTarget();

        Thread cacheRequests = new Thread(() -> {
            RedisService.cacheRequests(host, target);
        });
        cacheRequests.start();
    }

    private void handleConnectRequest(HttpRequest request, OutputStream clientOutputStream, InputStream clientInputStream) throws IOException {
        String host = request.getHeader().get("Host");
        String[] hostParts = host.split(":");
        String hostName = hostParts[0];
        int port = hostParts.length > 1 ? Integer.parseInt(hostParts[1]) : 443; // Mặc định cổng 443 nếu không có

        try (Socket tunnelSocket = new Socket(hostName, port)) {
            clientOutputStream.write("HTTP/1.1 200 Connection Established\r\n\r\n".getBytes());
            clientOutputStream.flush();

            // Chuyển tiếp dữ liệu giữa client và server
            InputStream finalInputStream = clientInputStream;
            Thread clientToServer = new Thread(() -> {
                try {
                    forwardData(finalInputStream, tunnelSocket.getOutputStream());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            OutputStream finalOutputStream = clientOutputStream;
            Thread serverToClient = new Thread(() -> {
                try {
                    forwardData(tunnelSocket.getInputStream(), finalOutputStream);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            clientToServer.start();
            serverToClient.start();

            clientToServer.join();
            serverToClient.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleHttpRequest(HttpRequest request, OutputStream clientOutputStream) throws IOException {
        String hostName = request.getHeader().get("Host");
        String[] hostParts = hostName.split(":");
        String host = hostParts[0];
        int port = hostParts.length > 1 ? Integer.parseInt(hostParts[1]) : 80; // Mặc định là cổng 80 cho HTTP

        // Tạo kết nối tới remote server
        try (Socket proxySocket = new Socket(host, port)) {
            OutputStream proxyOutputStream = proxySocket.getOutputStream();
            InputStream proxyInputStream = proxySocket.getInputStream();

            // Tạo request HTTP hợp lệ để gửi tới remote server
            StringBuilder httpRequest = new StringBuilder();
            httpRequest.append(request.getMethod()).append(" ").append(request.getRequestTarget()).append(" ").append(request.getBestCompatibleHttpVersion().LITERAL).append("\r\n");

            for (Map.Entry<String, String> header : request.getHeader().entrySet()) {
                httpRequest.append(header.getKey()).append(": ").append(header.getValue()).append("\r\n");
            }
            httpRequest.append("Connection: close\r\n"); // Đảm bảo kết nối được đóng sau khi nhận phản hồi
            httpRequest.append("\r\n");

            // Gửi request tới remote server
            System.out.println("Request to remote server: " + httpRequest.toString());
            proxyOutputStream.write(httpRequest.toString().getBytes());
            proxyOutputStream.flush();

            // Gửi body tới remote server nếu có
            // Chưa xử lý được trường hợp client thực hiện POST đăng nhập.

            forwardData(proxyInputStream, clientOutputStream);
        }
    }

    private void forwardData(InputStream input, OutputStream output) {
        byte[] buffer = new byte[4096];
        int bytesRead;
        try {
            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
                output.flush();
            }
        } catch (IOException e) {
        }
    }
}
