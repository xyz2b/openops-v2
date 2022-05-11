package com.openops.process;

import com.openops.common.ProtoInstant;
import com.openops.common.exception.InvalidMsgTypeException;
import com.openops.common.process.AbstractProcessor;
import com.openops.common.session.Session;
import com.openops.common.msg.ProtoMsgFactory.ProtoMsg;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommandExecuteResponseServerProcessor extends AbstractProcessor {
    public CommandExecuteResponseServerProcessor() {
        super(ProtoInstant.ProcessorType.COMMAND_EXECUTE_RESPONSE);
    }

    @Override
    public boolean action(Session session, Object message) throws InvalidMsgTypeException {
        if (message instanceof ProtoMsg.Message) {
            ProtoMsg.Message msg = (ProtoMsg.Message) message;

            checkMsgType(msg);

            ProtoMsg.CommandExecuteResponse commandExecuteResponse = msg.getCommandExecuteResponse();

            String uuid = commandExecuteResponse.getUuid();
            String host = commandExecuteResponse.getHost();
            // 执行结果写入redis, command:execute:response:host:uuid -> CommandExecuteResponse


        }
        return false;
    }

    private void checkMsgType(ProtoMsg.Message msg) throws InvalidMsgTypeException {
        if(msg.getType() != ProtoMsg.HeadType.COMMAND_EXECUTE_RESPONSE) {
            throw new InvalidMsgTypeException("消息类型错误");
        }
    }
}
