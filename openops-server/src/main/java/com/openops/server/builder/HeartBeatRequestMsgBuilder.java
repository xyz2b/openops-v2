package com.openops.server.builder;

import com.openops.common.Client;
import com.openops.common.builder.ProtoBufMsgBuilder;
import com.openops.common.msg.ProtoMsgFactory;

public class HeartBeatRequestMsgBuilder extends ProtoBufMsgBuilder {
    public HeartBeatRequestMsgBuilder(Client client) {
        super(ProtoMsgFactory.ProtoMsg.HeadType.HEARTBEAT_REQUEST, client.getSessionId());
    }

    @Override
    protected Object assembleMsgInner() {
        return null;
    }
}
