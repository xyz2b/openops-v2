package com.openops.server.session.entity;

import com.openops.server.distributed.Node;
import lombok.Data;

import java.io.Serializable;

@Data
public class SessionCache implements Serializable {
    private static final long serialVersionUID = -403010884211394856L;

    // 客户端的id
    private String clientId;
    // session id
    private String sessionId;

    // 节点信息
    private Node node;

    public SessionCache() {
        clientId = "";
        sessionId = "";
        node = new Node("unKnown", 0);
    }

    public SessionCache(String sessionId, String clientId, Node node) {
        this.sessionId = sessionId;
        this.clientId = clientId;
        this.node = node;
    }
}
