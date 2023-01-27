package org.idea.irpc.framework.core.netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class Server {
    private static EventLoopGroup bossGroup = null;
    private static EventLoopGroup workerGroup = null;

    public static void main(String[] args) throws InterruptedException {
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                //对应的是 tcp/ip 协议, listen函数中的 backlog 参数，用来初始化服务端可连接队列
                // backlog 用于构造服务端套接字 ServerSocket 对象，标识当服务器请求处理线程全满时，用于临时存放已完成三次握手的请求的队列的最大长度
                .option(ChannelOption.SO_BACKLOG, 1024)
                .option(ChannelOption.SO_SNDBUF, 16 * 1024)//发送缓冲区大小
                .option(ChannelOption.SO_RCVBUF, 16 * 1024) //接收缓存区大小
                .option(ChannelOption.SO_KEEPALIVE, true) //保持长连接
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        System.out.println("初始化连接通道信息,编解码处理器,定长处理器");
                        ch.pipeline().addLast(new StringEncoder());
                        //固定长度帧解码器
                        //ch.pipeline().addLast(new FixedLengthFrameDecoder(3));
                        //指定通过回车换行符来识别每次发送的数据，但是一旦当文本数据超过 maxLength 就会抛出异常
                        //ch.pipeline().addLast(new LineBasedFrameDecoder(5));
                        //指定特殊符号的分割处理
                        //ch.pipeline().addLast(new DelimiterBasedFrameDecoder(50, Unpooled.copiedBuffer("[end]".getBytes())));
                        ch.pipeline().addLast(new StringDecoder());
                        ch.pipeline().addLast(new ServerHandler());
                    }
                });
        bootstrap.bind(8080).sync();
        System.out.println("server is open");
    }
}
