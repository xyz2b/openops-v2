package com.openops.server.builder;

import com.openops.common.Client;
import com.openops.common.builder.ProtoBufMsgBuilder;
import com.openops.common.msg.ProtoMsgFactory;
import com.openops.common.msg.TerminalCommand;

public class CommandExecRequestMsgBuilder extends ProtoBufMsgBuilder {
    private TerminalCommand cmd;

    public CommandExecRequestMsgBuilder(Client client, TerminalCommand cmd) {
        super(ProtoMsgFactory.ProtoMsg.HeadType.COMMAND_EXECUTE_REQUEST, client.getSessionId());
        this.cmd = cmd;
    }

    @Override
    protected Object assembleMsgInner() {
        return ProtoMsgFactory.ProtoMsg.CommandExecuteRequest.newBuilder()
                .setCmd(cmd.getCmd())
                .setUser(cmd.getUser())
                .setTimeout(cmd.getTimeout())
                .setPriority(cmd.getPriority())
                .setUuid(cmd.getUuid())
                .setHost(cmd.getHost());
    }
}
