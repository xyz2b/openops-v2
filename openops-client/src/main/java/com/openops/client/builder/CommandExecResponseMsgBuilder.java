package com.openops.client.builder;

import com.openops.common.Client;
import com.openops.common.builder.ProtoBufMsgBuilder;
import com.openops.common.msg.ProtoMsgFactory;
import com.openops.common.msg.TerminalOutput;

public class CommandExecResponseMsgBuilder extends ProtoBufMsgBuilder {
    private TerminalOutput output;

    public CommandExecResponseMsgBuilder(Client client, TerminalOutput output) {
        super(ProtoMsgFactory.ProtoMsg.HeadType.COMMAND_EXECUTE_RESPONSE, client.getSessionId());
        this.output = output;
    }

    @Override
    protected Object assembleMsgInner() {
        return ProtoMsgFactory.ProtoMsg.CommandExecuteResponse.newBuilder()
                .setCode(output.getCode())
                .setStdout(output.getStdout())
                .setStderr(output.getStderr())
                .setInfo(output.getExtraInfo())
                .setUuid(output.getUuid())
                .setStart(output.getStart())
                .setEnd(output.getEnd())
                .setHost(output.getHost());
    }
}
