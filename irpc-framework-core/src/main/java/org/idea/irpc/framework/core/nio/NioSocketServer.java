package org.idea.irpc.framework.core.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class NioSocketServer extends Thread {
    ServerSocketChannel serverSocketChannel = null; //面向流的侦听套接字的可选通道
    Selector selector = null;//多路复用器
    SelectionKey selectionKey = null; //表示SelectableChannel向Selector注册的令牌

    public void initServer() throws IOException {
        selector = Selector.open();
        //打开服务器套接字通道, 新通道是通过调用默认 SelectorProvider 对象的 openServerSocketChannel 方法创建的
        serverSocketChannel = ServerSocketChannel.open();
        //设置为非阻塞模式，默认serverSocketChannel采用了阻塞模式
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.socket().bind(new InetSocketAddress(8888));
        //向给定选择器注册此通道，并返回选择键
        //sel–此通道要注册的选择器  ops–生成密钥的兴趣集
        selectionKey = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    @Override
    public void run() {
        while (true) {
            try {
                //默认这里会阻塞
                //选择一组键，其对应的通道已准备好进行IO操作; 返回值：已更新其就绪操作集的键数，可能为零
                int selectKey = selector.select();
                if (selectKey > 0) {
                    //获取到所有的处于就绪状态的channel，selectionKey中包含了channel的信息
                    Set<SelectionKey> keySet = selector.selectedKeys();
                    Iterator<SelectionKey> iter = keySet.iterator();
                    while (iter.hasNext()) {
                        SelectionKey selectionKey = iter.next();
                        //需要清空，防止下次重复处理
                        iter.remove();
                        //就绪事件，处理连接
                        if (selectionKey.isAcceptable()) {
                            accept(selectionKey);
                        }
                        //读事件，处理数据连接
                        if (selectionKey.isReadable()) {
                            read(selectionKey);
                        }
                        //写事件，处理写数据
                        if (selectionKey.isWritable()) {
                            write(selectionKey);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    serverSocketChannel.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    private void accept(SelectionKey key) {
        try {
            ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
            SocketChannel socketChannel = serverSocketChannel.accept();
            System.out.println("conn is acceptable");
            socketChannel.configureBlocking(false);
            //将当前的channel交给selector对象监管，并且有selector对象管理它的读事件
            socketChannel.register(selector, SelectionKey.OP_READ);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void read(SelectionKey selectionKey) {
        try {
            SocketChannel channel = (SocketChannel) selectionKey.channel();
            ByteBuffer byteBuffer = ByteBuffer.allocate(100);
            int len = channel.read(byteBuffer);
            if (len > 0) {
                byteBuffer.flip();
                byte[] byteArray = new byte[byteBuffer.limit()];
                byteBuffer.get(byteArray); //此方法将此缓冲区中的字节传输到给定的目标数组中
                System.out.println("NioSocketServer receive from client: " + new String(byteArray, 0, len));
                selectionKey.interestOps(SelectionKey.OP_READ); //将此键的兴趣集设置为给定值
            }
        } catch (Exception e) {
            try {
                serverSocketChannel.close();
                selectionKey.cancel();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
    }

    private void write(SelectionKey selectionKey) {
        SocketChannel channel = (SocketChannel) selectionKey.channel();
        try {
            channel.write(ByteBuffer.wrap("收到信息".getBytes()));
            selectionKey.interestOps(SelectionKey.OP_WRITE);
        } catch (IOException e) {
            try {
                serverSocketChannel.close();
                selectionKey.cancel();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
    }


    public static void main(String[] args) throws IOException {
        NioSocketServer server = new NioSocketServer();
        server.initServer();
        server.start();
    }
}
