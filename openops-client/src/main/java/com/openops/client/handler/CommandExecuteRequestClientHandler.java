package com.openops.client.handler;

import com.openops.client.process.CommandExecuteRequestClientProcessor;
import com.openops.client.session.ClientSession;
import com.openops.cocurrent.FutureTaskScheduler;
import com.openops.common.msg.ProtoMsgFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@ChannelHandler.Sharable
@Service("CommandExecuteRequestClientHandler")
public class CommandExecuteRequestClientHandler extends ChannelInboundHandlerAdapter {

    @Autowired
    CommandExecuteRequestClientProcessor commandExecuteRequestClientProcessor;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //判断消息实例
        if (null == msg || !(msg instanceof ProtoMsgFactory.ProtoMsg.Message)) {
            ctx.fireChannelRead(msg);
            return;
        }

        //判断消息类型
        ProtoMsgFactory.ProtoMsg.Message pkg = (ProtoMsgFactory.ProtoMsg.Message) msg;
        ProtoMsgFactory.ProtoMsg.HeadType headType = ((ProtoMsgFactory.ProtoMsg.Message) msg).getType();
        if (!headType.equals(ProtoMsgFactory.ProtoMsg.HeadType.COMMAND_EXECUTE_REQUEST)) {
            ctx.fireChannelRead(msg);
            return;
        }

        //异步处理转发的逻辑
        FutureTaskScheduler.add(() -> {
            ClientSession session = ClientSession.getSession(ctx);
            commandExecuteRequestClientProcessor.action(session, pkg);
            return null;
        });
    }

}

