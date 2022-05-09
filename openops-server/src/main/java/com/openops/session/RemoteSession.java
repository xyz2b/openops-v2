package com.openops.session;

import com.openops.distributed.Node;
import com.openops.distributed.PeerSender;
import com.openops.distributed.WorkerRouter;
import com.openops.session.entity.SessionCache;
import io.netty.channel.ChannelFuture;

import java.io.Serializable;

public class RemoteSession extends ServerSession implements Serializable {
    private static final long serialVersionUID = -400010884211394846L;

    private SessionCache cache;

    private boolean valid = true;

    public RemoteSession(SessionCache cache) {
        super(null);
    }

    @Override
    public String sessionId() {
        //委托
        return cache.getSessionId();
    }

    @Override
    public boolean isValid()
    {
        return valid;
    }

    public void setValid(boolean valid)
    {
        this.valid = valid;
    }

    public String getUserId() {
        //委托
        return cache.getClientId();
    }

    /**
     * 通过远程节点，转发
     */
    @Override
    public ChannelFuture writeAndFlush(Object msg) {
        Node node = cache.getNode();
        long nodeId = node.getId();

        //获取转发的sender
        PeerSender sender =
                WorkerRouter.getInst().route(nodeId);

        if(null != sender) {
            sender.writeAndFlush(msg);
        }

        return null;
    }
}
