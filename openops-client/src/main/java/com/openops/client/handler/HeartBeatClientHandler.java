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

    //在Handler被加入到Pipeline时，开始发送心跳
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        //发送心跳
        heartBeat(ctx);
    }

    private void heartBeat(ChannelHandlerContext ctx) {
        ClientSession session = ClientSession.getSession(ctx);
        Client client = session.client();
        Object message = new HeartBeatRequestMsgBuilder(client).build();

        ctx.executor().schedule(() -> {
            if (ctx.channel().isActive()) {
                log.info(" 发送 Node HEART_BEAT  消息 other");
                ctx.writeAndFlush(message);

                //递归调用，发送下一次的心跳
                heartBeat(ctx);
            }
        }, HEARTBEAT_INTERVAL, TimeUnit.SECONDS);
    }

    /**
     * 接受到服务器的心跳回写
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //判断消息实例
        if (null == msg || !(msg instanceof ProtoMsgFactory.ProtoMsg.Message)) {
            super.channelRead(ctx, msg);
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
