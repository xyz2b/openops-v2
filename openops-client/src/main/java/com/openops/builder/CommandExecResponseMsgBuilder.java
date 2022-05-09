package com.openops.builder;

import com.openops.common.Client;
import com.openops.common.builder.ProtoBufMsgBuilder;
import com.openops.common.msg.ProtoMsgFactory.ProtoMsg;
import com.openops.common.msg.TerminalOutput;
import com.google.protobuf.Timestamp;

public class CommandExecResponseMsgBuilder extends ProtoBufMsgBuilder {
    private TerminalOutput output;

    public CommandExecResponseMsgBuilder(Client client, TerminalOutput output) {
        super(ProtoMsg.HeadType.COMMAND_EXECUTE_RESPONSE, client.getClientId());
        this.output = output;
    }

    @Override
    protected Object assembleMsgInner() {
        return ProtoMsg.CommandExecuteResponse.newBuilder()
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
