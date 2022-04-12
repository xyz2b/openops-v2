package com.openops.builder;

import com.openops.common.Client;
import com.openops.common.builder.ProtoBufMsgBuilder;
import com.openops.common.msg.ProtoMsgFactory.ProtoMsg;

public class HeartBeatRequestMsgBuilder extends ProtoBufMsgBuilder {
    public HeartBeatRequestMsgBuilder(Client client) {
        super(ProtoMsg.HeadType.HEARTBEAT_REQUEST, client.getClientId());
    }

    @Override
    protected Object assembleMsgInner() {
        return null;
    }

    public static void main(String[] args) {
        HeartBeatRequestMsgBuilder builder = new HeartBeatRequestMsgBuilder(new Client("111", "222", "333", "4444"));
        ProtoMsg.Message message = (ProtoMsg.Message) builder.build();
        System.out.println(message.getType());
    }
}