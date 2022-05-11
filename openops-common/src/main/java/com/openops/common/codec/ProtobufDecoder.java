package com.openops.common.codec;

import com.openops.common.ProtoInstant;
import com.openops.common.exception.InvalidFrameException;
import com.openops.common.msg.ProtoMsgFactory.ProtoMsg;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

public class ProtobufDecoder extends LengthFieldBasedFrameDecoder {
    public ProtobufDecoder() {
        super(Integer.MAX_VALUE, 8, 4, 0, 0);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf frame = (ByteBuf) super.decode(ctx, in);
        if (frame == null) {
            return null;
        }

        //读取魔数
        int magic = frame.readInt();
        if (magic != ProtoInstant.MAGIC_CODE) {
            String error = "客户端口令不对:" + ctx.channel().remoteAddress();
            throw new InvalidFrameException(error);
        }

        //读取版本
        int version = frame.readInt();

        // 读取传送过来的消息的长度。
        int length = frame.readInt();

        // 长度如果小于0
        if (length < 0) {
            // 非法数据，关闭连接
            ctx.close();
        }

        byte[] buffer;
        if (frame.hasArray()) {
            // 堆内存缓冲
            ByteBuf slice = frame.slice(frame.readerIndex(), length);
            buffer = slice.array();
            frame.retain();

        } else {
            // 直接内存缓冲
            buffer = new byte[length];
            frame.readBytes(buffer, 0, length);
        }

        // 字节转成对象
        ProtoMsg.Message msg =
                ProtoMsg.Message.parseFrom(buffer);
        if (frame.hasArray()) {
            frame.release();
        }

        return msg;
    }
}
