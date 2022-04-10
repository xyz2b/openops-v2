package com.openops.builder;

import com.openops.common.Client;
import com.openops.common.builder.ProtoBufMsgBuilder;
import com.openops.common.msg.ProtoMsgFactory.ProtoMsg;

public class HeartBeatResponseMsgBuilder extends ProtoBufMsgBuilder {
    public HeartBeatResponseMsgBuilder(Client client) {
        super(ProtoMsg.HeadType.HEARTBEAT_RESPONSE, client.getClientId());
    }

    @Override
    protected Object assembleMsgInner() {
        return null;
    }

    public static void main(String[] args) {
        HeartBeatResponseMsgBuilder builder = new HeartBeatResponseMsgBuilder(new Client("111", "222", "333", "4444"));
        ProtoMsg.Message message = (ProtoMsg.Message) builder.build();
        System.out.println(message.getType());
    }
}
