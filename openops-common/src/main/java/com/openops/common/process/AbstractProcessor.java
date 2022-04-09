package com.openops.common.process;

import com.openops.common.msg.ProtoMsgFactory.ProtoMsg;

public abstract class AbstractProcessor implements Processor {
    private final ProtoMsg.HeadType type;

    public AbstractProcessor(ProtoMsg.HeadType type) {
        this.type = type;
    }

    public ProtoMsg.HeadType type() {
        return type;
    }
}
