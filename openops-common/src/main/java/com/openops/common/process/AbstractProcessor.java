package com.openops.common.process;

import com.openops.common.msg.ProtoMsgFactory.ProtoMsg;

public abstract class AbstractProcessor implements Processor {
    private final int type;

    public AbstractProcessor(int type) {
        this.type = type;
    }

    public int type() {
        return type;
    }
}
