package org.idea.irpc.framework.core.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

import static org.idea.irpc.framework.core.common.constants.RpcConstants.MAGIC_NUMBER;

//RPC解码器
public class RpcDecoder extends ByteToMessageDecoder {
    //协议开头部分的标准长度
    public final int BASE_LENGTH = 2 + 4;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> out) throws Exception {
        if (byteBuf.readableBytes() > BASE_LENGTH) {
            //防止收到一些体积过大的数据包，目前限制在1000大小，后期版本这里是可配置模式
            if (byteBuf.readableBytes() > 1000) {
                byteBuf.skipBytes(byteBuf.readableBytes());//将此缓冲区中的当前readerIndex增加指定长度 就是跳过
            }
            int beginReader;
            while (true) {
                beginReader = byteBuf.readerIndex();
                byteBuf.markReaderIndex();//标记此缓冲区中的当前readerIndex
                if (byteBuf.readShort() == MAGIC_NUMBER) { //此处 2 个字节
                    break;
                } else {
                    //不是魔数开头，说明是非法的客户端发来的数据包
                    ctx.close();
                    return;
                }
            }

            int length = byteBuf.readInt(); //此处 4 个字节表明发送数据的长度
            //说明剩余的数据包不是完整的，这里需要重置下读索引
            if (byteBuf.readableBytes() < length) {
                byteBuf.readerIndex(beginReader);
                return;
            }
            byte[] data = new byte[length];
            byteBuf.readBytes(data);
            RpcProtocol rpcProtocol = new RpcProtocol(data);
            out.add(rpcProtocol);
        }
    }
}
