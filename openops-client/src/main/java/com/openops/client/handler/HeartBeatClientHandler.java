package com.openops.client.handler;

import com.openops.common.Client;
import com.openops.common.msg.ProtoMsgFactory;
import com.openops.client.builder.HeartBeatRequestMsgBuilder;
import com.openops.client.session.ClientSession;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@ChannelHandler.Sharable
@Service("HeartBeatClientHandler")
public class HeartBeatClientHandler extends ChannelInboundHandlerAdapter {
    //心跳的时间间隔，单位为s
    private static final int HEARTBEAT_INTERVAL = 30;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ClientSession session = ClientSession.getSession(ctx);
        Client client = session.client();
        Object message = new HeartBeatRequestMsgBuilder(client).build();

        //发送心跳
        heartBeat(ctx, message);
    }

    private void heartBeat(ChannelHandlerContext ctx, Object message) {
        ctx.executor().scheduleAtFixedRate(() -> {
            if (ctx.channel().isActive()) {
                ctx.writeAndFlush(message);
                log.info("{} 发送 HEART_BEAT to {}", ctx.channel().localAddress().toString(), ctx.channel().remoteAddress().toString());
            }
        }, 0, HEARTBEAT_INTERVAL, TimeUnit.SECONDS);
    }

    /**
     * 接受到服务器的心跳回写
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.info("收到 Node HEART_BEAT  消息 from: " + ctx.channel().remoteAddress().toString());

        //判断消息实例
        if (null == msg || !(msg instanceof ProtoMsgFactory.ProtoMsg.Message)) {
            ctx.fireChannelRead(msg);
            return;
        }

        //判断类型
        ProtoMsgFactory.ProtoMsg.Message pkg = (ProtoMsgFactory.ProtoMsg.Message) msg;
        ProtoMsgFactory.ProtoMsg.HeadType headType = pkg.getType();
        if (headType.equals(ProtoMsgFactory.ProtoMsg.HeadType.HEARTBEAT_RESPONSE)) {
            log.info("  收到 Node HEART_BEAT  消息 from: " + ctx.channel().remoteAddress().toString());
            log.info("  收到 Node HEART_BEAT seq: " + pkg.getSequence());
        } else {
            ctx.fireChannelRead(msg);
        }
    }

}
