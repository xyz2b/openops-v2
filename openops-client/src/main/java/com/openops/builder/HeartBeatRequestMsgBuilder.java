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
}
