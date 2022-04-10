package com.openops.builder;

import com.openops.common.Client;
import com.openops.common.builder.ProtoBufMsgBuilder;
import com.openops.common.msg.ProtoMsgFactory.ProtoMsg;
import com.openops.common.msg.TerminalOutput;

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
                .setInfo(output.getExtraInfo());
    }

    public static void main(String[] args) {
        CommandExecResponseMsgBuilder builder = new CommandExecResponseMsgBuilder(new Client("111", "222", "333", "4444"), new TerminalOutput(0, "success", "", ""));
        ProtoMsg.Message message = (ProtoMsg.Message) builder.build();
        System.out.println(message.getCommandExecuteResponse().getStdout());
    }
}
