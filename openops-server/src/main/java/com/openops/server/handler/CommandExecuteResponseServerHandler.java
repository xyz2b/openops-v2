package com.openops.server.handler;

import com.openops.common.msg.ProtoMsgFactory;
import com.openops.cocurrent.FutureTaskScheduler;
import com.openops.server.process.CommandExecuteResponseServerProcessor;
import com.openops.server.session.LocalSession;
import com.openops.server.session.service.SessionManager;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service("CommandExecuteResponseServerHandler")
@ChannelHandler.Sharable
public class CommandExecuteResponseServerHandler extends ChannelInboundHandlerAdapter
{

    @Autowired
    CommandExecuteResponseServerProcessor commandExecuteResponseServerProcessor;

    @Autowired
    SessionManager sessionManager;

    /**
     * 收到消息
     */
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception
    {
        //判断消息实例
        if (null == msg || !(msg instanceof ProtoMsgFactory.ProtoMsg.Message)) {
            ctx.fireChannelRead(msg);
            return;
        }

        //判断消息类型
        ProtoMsgFactory.ProtoMsg.Message pkg = (ProtoMsgFactory.ProtoMsg.Message) msg;
        ProtoMsgFactory.ProtoMsg.HeadType headType = ((ProtoMsgFactory.ProtoMsg.Message) msg).getType();
        if (!headType.equals(ProtoMsgFactory.ProtoMsg.HeadType.COMMAND_EXECUTE_RESPONSE)) {
            ctx.fireChannelRead(msg);
            return;
        }

        //异步处理转发的逻辑
        FutureTaskScheduler.add(() -> {
            LocalSession session = LocalSession.getSession(ctx);
            commandExecuteResponseServerProcessor.action(session, pkg);
            return null;
        });
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LocalSession session = LocalSession.getSession(ctx);

        if (null != session && session.isValid()) {
            session.unbind();
        }
    }
}
