package org.idea.irpc.framework.core.nio;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class NioSocketClient {
    public static void main(String[] args) {
        SocketChannel socketChannel = null;
        try {
            socketChannel = SocketChannel.open();
            socketChannel.connect(new InetSocketAddress(InetAddress.getLocalHost(), 8888));
            socketChannel.configureBlocking(false);
            while (true) {
                socketChannel.write(ByteBuffer.wrap(("this is test" + Thread.currentThread().getName()).getBytes()));
                Thread.sleep(2000);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
