package com.openops.common.builder;

import com.openops.common.msg.ProtoMsgFactory.ProtoMsg;

public abstract class ProtoBufMsgBuilder extends AbstractMsgBuilder {

    protected ProtoBufMsgBuilder(int type, String clientId) {
        super(type, clientId);
    }

    protected ProtoMsg.Message buildMsgOuter(long sequence) {
        this.sequence = sequence;

        ProtoMsg.Message.Builder mb =
                ProtoMsg.Message
                        .newBuilder()
                        .setType(ProtoMsg.HeadType.forNumber(type))
                        .setSessionId(clientId)
                        .setSequence(sequence);
        return mb.buildPartial();
    }

    protected abstract Object buildInner();
}
