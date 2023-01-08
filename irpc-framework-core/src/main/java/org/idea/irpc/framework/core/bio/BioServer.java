package org.idea.irpc.framework.core.bio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BioServer {

    private static ExecutorService executorService = Executors.newFixedThreadPool(10);

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket();
        serverSocket.bind(new InetSocketAddress(1009));
        try {
            while (true) {
                //堵塞状态点 --1
                Socket socket = serverSocket.accept();
                System.out.println("获取新连接");
                executorService.execute(() -> {
                    while (true) {
                        InputStream inputStream = null;
                        try {
                            //堵塞状态点 --2
                            inputStream = socket.getInputStream();
                            byte[] result = new byte[1024];
                            int len = inputStream.read(result);
                            if (len != -1) {
                                //String构造方法
                                //bytes – 要解码为字符的字节，offset – 要解码的第一个字节的索引，length – 要解码的字节数
                                System.out.println("[response]" + new String(result, 0, len));
                                OutputStream outputStream = socket.getOutputStream();
                                outputStream.write("response data".getBytes());
                                //flush调用它表示，如果输出流的实现缓冲了先前写入的任何字节，则应立即将这些字节写入其预期目的地
                                outputStream.flush();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            break;
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
