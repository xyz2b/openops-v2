package com.openops.client.builder;

import com.openops.common.Client;
import com.openops.common.builder.ProtoBufMsgBuilder;
import com.openops.common.msg.ProtoMsgFactory;

public class AuthRequestMsgBuilder extends ProtoBufMsgBuilder {
    private Client client;

    public AuthRequestMsgBuilder(Client client) {
        super(ProtoMsgFactory.ProtoMsg.HeadType.AUTH_REQUEST, client.getSessionId());
        this.client = client;
    }

    @Override
    protected Object assembleMsgInner() {
        return ProtoMsgFactory.ProtoMsg.AuthRequest.newBuilder()
                .setPlatform(client.getPlatform())
                .setToken(client.getToken())
                .setClientVersion(client.getClientVersion())
                .setClientId(client.getClientId());
    }
}
