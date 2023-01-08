package org.idea.irpc.framework.core.bio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BioClient {
    private static ExecutorService executorService = Executors.newFixedThreadPool(1);

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress(1009));
        OutputStream outputStream = null;
        while (true) {
            //输入需要发送的消息
            Scanner scanner = new Scanner(System.in);
            String nextLine = scanner.nextLine();
            outputStream = socket.getOutputStream();
            outputStream.write(nextLine.getBytes());
            outputStream.flush();
            System.out.println("发送结束");
            executorService.execute(() -> {
                try {
                    InputStream inputStream = socket.getInputStream();
                    byte[] response = new byte[1024];
                    //这里会阻塞等待数据从服务端抵达网卡缓冲区，然后才能将内核态的数据拷贝到用户态中
                    int len = inputStream.read(response);
                    if (len != -1) {
                        System.out.println("获取到数据:" + new String(response, 0, len));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
