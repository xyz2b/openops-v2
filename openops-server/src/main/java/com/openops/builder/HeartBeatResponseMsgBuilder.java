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
}
