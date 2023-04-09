package com.github.paicoding.forum.test.javabetter.socket1.httpserver;

import lombok.extern.slf4j.Slf4j;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

@Slf4j
public class BasicHttpServer {
    private static ExecutorService bootstrapExecutor = Executors.newSingleThreadExecutor();
    private static ExecutorService taskExecutor;
    private static final String PORT_NAME = "quick.fix.port";

    static void startHttpServer() {
        int nThreads = Runtime.getRuntime().availableProcessors();
        taskExecutor =
                new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(100),
                        new ThreadPoolExecutor.DiscardPolicy());

        int port = Integer.parseInt(System.getProperty(PORT_NAME, "9999"));
        while (true) {
            try {

                ServerSocket serverSocket = new ServerSocket(port);
                bootstrapExecutor.submit(new ServerThread(serverSocket));
                log.info("FixEndPoint Port: {}", port);
                break;
            } catch (Exception e) {
                try {
                    //重试
                    TimeUnit.SECONDS.sleep(10);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        bootstrapExecutor.shutdown();
    }

    private static class ServerThread implements Runnable {

        private ServerSocket serverSocket;

        public ServerThread(ServerSocket s) {
            this.serverSocket = s;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    Socket socket = this.serverSocket.accept();
                    HttpTask eventTask = new HttpTask(socket);
                    taskExecutor.submit(eventTask);
                } catch (Exception e) {
                    log.error("Some Error In Default ServletFixEndPoint! {}", e);
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
    }
}
