package com.openops.server.handler;

import com.openops.common.ServerConstants;
import com.openops.common.msg.ProtoMsgFactory;
import com.openops.server.builder.HeartBeatResponseMsgBuilder;
import com.openops.server.session.LocalSession;
import com.openops.server.session.service.SessionManager;
import com.openops.cocurrent.FutureTaskScheduler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service("HeartBeatServerHandler")
public class HeartBeatServerHandler extends IdleStateHandler {
    private static final int READ_IDLE_GAP = 30;

    public HeartBeatServerHandler() {
        super(READ_IDLE_GAP, 0, 0, TimeUnit.SECONDS);
    }

    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //判断消息实例
        if (null == msg || !(msg instanceof ProtoMsgFactory.ProtoMsg.Message)) {
            super.channelRead(ctx, msg);
            return;
        }

        ProtoMsgFactory.ProtoMsg.Message pkg = (ProtoMsgFactory.ProtoMsg.Message) msg;
        //判断消息类型
        ProtoMsgFactory.ProtoMsg.HeadType headType = pkg.getType();
        if (headType.equals(ProtoMsgFactory.ProtoMsg.HeadType.HEARTBEAT_REQUEST)) {
            //异步处理,将心跳包，直接回复给客户端
            FutureTaskScheduler.add(() -> {
                if (ctx.channel().isActive()) {
                    Object message = new HeartBeatResponseMsgBuilder(LocalSession.getSession(ctx).client());
                    ctx.writeAndFlush(message);
                }
                return null;
            });
        }
        ctx.fireChannelRead(msg);
    }

    @Override
    protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) throws Exception {
        log.info(READ_IDLE_GAP + "秒内未读到数据，关闭连接 {}", ctx.channel().attr(ServerConstants.CHANNEL_NAME).get());
        SessionManager.getSessionManger().closeSession(ctx);
    }
}