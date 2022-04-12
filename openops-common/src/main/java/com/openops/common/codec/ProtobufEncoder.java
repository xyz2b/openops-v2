package com.openops.common.codec;

import com.openops.common.ProtoInstant;
import com.openops.common.msg.ProtoMsgFactory.ProtoMsg;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class ProtobufEncoder extends MessageToByteEncoder<ProtoMsg.Message> {
    @Override
    protected void encode(ChannelHandlerContext ctx, ProtoMsg.Message msg, ByteBuf out) throws Exception {
        out.writeInt(ProtoInstant.MAGIC_CODE);
        out.writeInt(ProtoInstant.VERSION_CODE);

        // 将对象转换为byte
        byte[] msgBytes = msg.toByteArray();

        int length = msgBytes.length;// 读取消息的长度

        // 先将消息长度写入，也就是消息头
        out.writeInt(length);

        // 消息体中包含我们要发送的数据
        out.writeBytes(msgBytes);
    }
}
