package com.openops.server.builder;

import com.openops.common.builder.ProtoBufMsgBuilder;
import com.openops.common.msg.ProtoMsgFactory;
import com.openops.common.Client;

public class HeartBeatResponseMsgBuilder extends ProtoBufMsgBuilder {
    public HeartBeatResponseMsgBuilder(Client client) {
        super(ProtoMsgFactory.ProtoMsg.HeadType.HEARTBEAT_RESPONSE, client.getSessionId());
    }

    @Override
    protected Object assembleMsgInner() {
        return null;
    }
}
