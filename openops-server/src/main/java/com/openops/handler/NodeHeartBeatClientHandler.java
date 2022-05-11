package com.openops.handler;

import com.openops.builder.HeartBeatRequestMsgBuilder;
import com.openops.common.Client;
import com.openops.common.msg.ProtoMsgFactory.ProtoMsg;
import com.openops.distributed.Node;
import com.openops.distributed.Worker;
import com.openops.session.ClientSession;
import com.openops.util.JsonUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@ChannelHandler.Sharable
@Service("NodeHeartBeatClientHandler")
public class NodeHeartBeatClientHandler extends ChannelInboundHandlerAdapter {
    //心跳的时间间隔，单位为s
    private static final int HEARTBEAT_INTERVAL = 50;

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
}
