package com.openops.common.sender;

public interface Sender {
    void send(Object message);
    boolean isValid();
    boolean isConnected();
}
