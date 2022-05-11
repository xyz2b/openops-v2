package com.openops.process;

import com.openops.common.ProtoInstant;
import com.openops.common.exception.InvalidMsgTypeException;
import com.openops.common.msg.ProtoMsgFactory.ProtoMsg;
import com.openops.common.process.AbstractProcessor;
import com.openops.common.session.Session;
import com.openops.session.LocalSession;
import com.openops.session.RemoteSession;
import com.openops.session.service.SessionManager;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommandExecuteRequestServerProcessor extends AbstractProcessor {
    public CommandExecuteRequestServerProcessor() {
        super(ProtoInstant.ProcessorType.COMMAND_EXECUTE_REQUEST);
    }

    @Override
    public boolean action(Session fromSession, Object message) throws InvalidMsgTypeException {
        if (message instanceof ProtoMsg.Message) {
            ProtoMsg.Message msg = (ProtoMsg.Message) message;

            checkMsgType(msg);

            ProtoMsg.CommandExecuteRequest commandExecuteRequest = msg.getCommandExecuteRequest();

            String to = commandExecuteRequest.getHost();

            Session toSession = SessionManager.getSessionManger().getSessionByClientId(to);
            if (null != fromSession && fromSession.isLogin()) {
                boolean isRemoteSession = false;
                if(toSession instanceof RemoteSession) {
                    isRemoteSession = true;
                }

                if (null != toSession && toSession.isValid()) {
                    toSession.writeAndFlush(msg);
                    return true;
                } else {
                    // 接收方离线
                    log.error("[" + to + "] 不在线!，类型: ", isRemoteSession ? "remote" : "local");
                    return false;
                }
            } else if (null != fromSession && !fromSession.isLogin()) {
                // 发送方未登录
                log.error("发送方 [" + fromSession.client().getClientId() + "] 未登录!");
                return false;
            }
        }
        return false;
    }

    private void checkMsgType(ProtoMsg.Message msg) throws InvalidMsgTypeException {
        if(msg.getType() != ProtoMsg.HeadType.COMMAND_EXECUTE_REQUEST) {
            throw new InvalidMsgTypeException("消息类型错误");
        }
    }
}
