package com.openops.common.process;

import com.openops.common.ProtoInstant;

public abstract class AbstractProcessor implements Processor {
    private final ProtoInstant.ProcessorType type;

    protected AbstractProcessor(ProtoInstant.ProcessorType type) {
        this.type = type;
    }

    public ProtoInstant.ProcessorType type() {
        return type;
    }
}
