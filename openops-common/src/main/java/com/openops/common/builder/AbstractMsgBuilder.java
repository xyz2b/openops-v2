package com.openops.common.builder;

public abstract class AbstractMsgBuilder implements MsgBuilder {

    protected abstract Object buildMsg();

    public Object build() {
        return buildMsg();
    }
}
