package com.openops.server.process;

import com.openops.common.msg.ProtoMsgFactory;
import com.openops.common.process.AbstractProcessor;
import com.openops.common.session.Session;
import com.openops.common.ProtoInstant;
import com.openops.common.exception.InvalidMsgTypeException;
import com.openops.server.process.dao.CommandExecuteResponseDAO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository("CommandExecuteResponseServerProcessor")
public class CommandExecuteResponseServerProcessor extends AbstractProcessor {
    @Autowired
    CommandExecuteResponseDAO commandExecuteResponseDAO;

    public CommandExecuteResponseServerProcessor() {
        super(ProtoInstant.ProcessorType.COMMAND_EXECUTE_RESPONSE);
    }

    @Override
    public boolean action(Session session, Object message) throws InvalidMsgTypeException {
        if (message instanceof ProtoMsgFactory.ProtoMsg.Message) {
            ProtoMsgFactory.ProtoMsg.Message msg = (ProtoMsgFactory.ProtoMsg.Message) message;

            checkMsgType(msg);

            ProtoMsgFactory.ProtoMsg.CommandExecuteResponse commandExecuteResponse = msg.getCommandExecuteResponse();

            // 执行结果写入redis, command:execute:response:host:uuid -> CommandExecuteResponse
            commandExecuteResponseDAO.save(commandExecuteResponse);
        }
        return false;
    }

    private void checkMsgType(ProtoMsgFactory.ProtoMsg.Message msg) throws InvalidMsgTypeException {
        if(msg.getType() != ProtoMsgFactory.ProtoMsg.HeadType.COMMAND_EXECUTE_RESPONSE) {
            throw new InvalidMsgTypeException("消息类型错误");
        }
    }
}
