package com.openops.server.process;

import com.openops.common.msg.ProtoMsgFactory;
import com.openops.common.process.AbstractProcessor;
import com.openops.common.session.Session;
import com.openops.common.ProtoInstant;
import com.openops.common.exception.InvalidMsgTypeException;
import com.openops.server.session.RemoteSession;
import com.openops.server.session.service.SessionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service("CommandExecuteRequestServerProcessor")
public class CommandExecuteRequestServerProcessor extends AbstractProcessor {
    public CommandExecuteRequestServerProcessor() {
        super(ProtoInstant.ProcessorType.COMMAND_EXECUTE_REQUEST);
    }

    @Override
    public boolean action(Session fromSession, Object message) throws InvalidMsgTypeException {
        if (message instanceof ProtoMsgFactory.ProtoMsg.Message) {
            ProtoMsgFactory.ProtoMsg.Message msg = (ProtoMsgFactory.ProtoMsg.Message) message;

            checkMsgType(msg);

            ProtoMsgFactory.ProtoMsg.CommandExecuteRequest commandExecuteRequest = msg.getCommandExecuteRequest();

            // TODO: 批量处理对多个客户端执行相同的命令，将相同节点管理的Node的客户端的请求集中到一个报文中，避免发送很多报文
            List<String> tos = commandExecuteRequest.getHostList();
            boolean allSuccess = true;
            for(String to : tos) {
                Session toSession = SessionManager.getSessionManger().getSessionByClientId(to);
                if (null != fromSession && fromSession.isLogin()) {
                    boolean isRemoteSession = false;
                    if(toSession instanceof RemoteSession) {
                        isRemoteSession = true;
                    }

                    if (null != toSession && toSession.isValid()) {
                        toSession.send(msg);
                    } else {
                        // 接收方离线
                        log.error("接收方 [{}] 不在线!，类型: {}.", to, isRemoteSession ? "remote" : "local");
                        allSuccess = false;
                    }
                } else if (null != fromSession && !fromSession.isLogin()) {
                    // 发送方未登录
                    log.error("发送方 [{}] 未登录!", fromSession.client().getClientId());
                    allSuccess = false;
                }
            }
            return allSuccess;
        }
        return false;
    }

    private void checkMsgType(ProtoMsgFactory.ProtoMsg.Message msg) throws InvalidMsgTypeException {
        if(msg.getType() != ProtoMsgFactory.ProtoMsg.HeadType.COMMAND_EXECUTE_REQUEST) {
            throw new InvalidMsgTypeException("消息类型错误");
        }
    }
}
