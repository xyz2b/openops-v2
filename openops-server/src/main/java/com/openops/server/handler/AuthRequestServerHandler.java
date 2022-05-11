package com.openops.server.handler;

import com.openops.common.msg.ProtoMsgFactory;
import com.openops.server.process.AuthRequestProcessor;
import com.openops.server.session.LocalSession;
import com.openops.server.session.service.SessionManager;
import com.openops.cocurrent.CallbackTask;
import com.openops.cocurrent.CallbackTaskScheduler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service("AuthRequestServerHandler")
@ChannelHandler.Sharable
public class AuthRequestServerHandler extends ChannelInboundHandlerAdapter {
    @Autowired
    AuthRequestProcessor authRequestProcessor;

    /**
     * 收到消息
     */
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (null == msg || !(msg instanceof ProtoMsgFactory.ProtoMsg.Message)) {
            ctx.fireChannelRead(msg);
            return;
        }

        ProtoMsgFactory.ProtoMsg.Message pkg = (ProtoMsgFactory.ProtoMsg.Message) msg;

        //取得请求类型
        ProtoMsgFactory.ProtoMsg.HeadType headType = pkg.getType();

        if (!headType.equals(ProtoMsgFactory.ProtoMsg.HeadType.AUTH_REQUEST)) {
            ctx.fireChannelRead(msg);
            return;
        }

        // 将Session、Channel关联
        LocalSession session = new LocalSession(ctx.channel());

        //异步任务，处理登录的逻辑
        CallbackTaskScheduler.add(new CallbackTask<Boolean>() {
            //异步任务返回
            @Override
            public void onSuccess(Boolean r) {
                if (r) {
                    log.info("认证成功:" + ctx.channel().remoteAddress().toString());

                    ctx.pipeline().addAfter("login", "heartBeat", new HeartBeatServerHandler());
                    ctx.pipeline().remove("login");
                } else {
                    SessionManager.getSessionManger().closeSession(ctx);

                    log.info("认证失败:" + ctx.channel().remoteAddress().toString());

                }
            }

            //异步任务异常
            @Override
            public void onFailure(Throwable t) {
                t.printStackTrace();
                log.info("认证失败:" + ctx.channel().remoteAddress().toString());
                SessionManager.getSessionManger().closeSession(ctx);
            }

            @Override
            public Boolean execute() throws Exception {
                return authRequestProcessor.action(session, pkg);
            }
        });

    }

}
