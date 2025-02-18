package core;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class ServerListenerThread extends Thread {
    private final static Logger logger = Logger.getLogger(ServerListenerThread.class.getName());
    private int port;
    private String webroot;
    private ServerSocket serverSocket;
    private ExecutorService threadPool = Executors.newFixedThreadPool(100);

    public ServerListenerThread(int port, String webroot) throws IOException {
        this.port = port;
        this.webroot = webroot;
        this.serverSocket = new ServerSocket(this.port);
    }

    @Override
    public void run() {
        try {
            while (serverSocket.isBound() && !serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();

                logger.info("Connection accepted: " + socket.getInetAddress());

                HttpConnectionWorkerThread workerThread = new HttpConnectionWorkerThread(socket);
                threadPool.submit(workerThread);
            }
        } catch (IOException e) {
            logger.info("Problem with setting socket, " + e);
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
