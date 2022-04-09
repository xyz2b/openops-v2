package com.openops.common.builder;

public abstract class AbstractMsgBuilder implements MsgBuilder {
    protected final int type;
    protected long sequence;
    protected final String clientId;

    protected abstract Object buildMsg();

    protected AbstractMsgBuilder(int type, String clientId) {
        this.type = type;
        this.clientId = clientId;
    }

    public Object build() {
        return buildMsg();
    }
}
