package com.openops.server.handler;

import com.openops.common.ServerConstants;
import com.openops.common.msg.ProtoMsgFactory;
import com.openops.server.builder.HeartBeatResponseMsgBuilder;
import com.openops.server.process.FlushClientSessionProcessor;
import com.openops.server.session.LocalSession;
import com.openops.server.session.ServerSession;
import com.openops.server.session.service.SessionManager;
import com.openops.cocurrent.FutureTaskScheduler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service("HeartBeatServerHandler")
@ChannelHandler.Sharable
public class HeartBeatServerHandler extends IdleStateHandler {
    @Autowired
    FlushClientSessionProcessor flushClientSessionProcessor;

    private static final int READ_IDLE_GAP = 60;

    public HeartBeatServerHandler() {
        super(READ_IDLE_GAP, 0, 0, TimeUnit.SECONDS);
    }

    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //判断消息实例
        if (null == msg || !(msg instanceof ProtoMsgFactory.ProtoMsg.Message)) {
            ctx.fireChannelRead(msg);
            return;
        }

        ProtoMsgFactory.ProtoMsg.Message pkg = (ProtoMsgFactory.ProtoMsg.Message) msg;
        //判断消息类型
        ProtoMsgFactory.ProtoMsg.HeadType headType = pkg.getType();
        if (headType.equals(ProtoMsgFactory.ProtoMsg.HeadType.HEARTBEAT_REQUEST)) {
            log.info("收到客户端的心跳包: {}", ctx.channel().remoteAddress().toString());
            //异步处理,将心跳包，直接回复给客户端
            FutureTaskScheduler.add(() -> {
                if (ctx.channel().isActive()) {
                    LocalSession localSession = LocalSession.getSession(ctx);
                    Object message = new HeartBeatResponseMsgBuilder(localSession.client()).build();
                    ctx.writeAndFlush(message);
                    log.info("回复客户端心跳包: {}", ctx.channel().remoteAddress().toString());

                    // 刷新客户端的session
                    log.info("flush redis sessionId: {}, clientId: {}", localSession.sessionId(), localSession.client().getClientId());
                    flushClientSessionProcessor.action(localSession, msg);
                }
                return null;
            });

            // 如果不把消息传递给IdleStateHandler，IdleStateHandler会认为没有收到消息，就不会更新idle read计时器
            super.channelRead(ctx, msg);
        } else {
            ctx.fireChannelRead(msg);
        }

    }

    @Override
    protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) throws Exception {
        LocalSession localSession = LocalSession.getSession(ctx);
        log.info(READ_IDLE_GAP + "秒内未读到数据，关闭连接 {}", localSession.clientId());
        SessionManager.getSessionManger().closeSession(ctx);
    }
}