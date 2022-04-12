package com.openops.builder;

import com.openops.common.Client;
import com.openops.common.builder.ProtoBufMsgBuilder;
import com.openops.common.msg.ProtoMsgFactory.ProtoMsg;
import com.openops.common.msg.TerminalCommand;

public class CommandExecRequestMsgBuilder extends ProtoBufMsgBuilder {
    private TerminalCommand cmd;

    public CommandExecRequestMsgBuilder(Client client, TerminalCommand cmd) {
        super(ProtoMsg.HeadType.COMMAND_EXECUTE_REQUEST, client.getClientId());
        this.cmd = cmd;
    }

    @Override
    protected Object assembleMsgInner() {
        return ProtoMsg.CommandExecuteRequest.newBuilder()
                .setCmd(cmd.getCmd())
                .setUser(cmd.getUser())
                .setTimeout(cmd.getTimeout())
                .setPriority(cmd.getPriority())
                .setUuid(cmd.getUuid())
                .setHost(cmd.getHost());
    }
}
