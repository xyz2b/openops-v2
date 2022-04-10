package com.openops.session;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class SessionManger {
    private static SessionManger singleInstance = null;

    public static SessionManger inst() {
        return singleInstance;
    }

    public static void setSingleInstance(SessionManger singleInstance) {
        SessionManger.singleInstance = singleInstance;
    }

    private ConcurrentHashMap<String, ServerSession> sessionMap = new ConcurrentHashMap<String, ServerSession>();

    public void addSession(ServerSession session) {
        sessionMap.put(session.clientId(), session);

        // TODO: 更新zk server节点，新增client
    }

    public ServerSession getSession(String clientId) {
        ServerSession serverSession = sessionMap.get(clientId);
        if (null == serverSession) {
            log.info("client：{} 下线了? 没有任何会话 ", clientId);
            return null;
        }
        return serverSession;
    }

    public void closeSession(ChannelHandlerContext ctx)
    {

        ServerSession session =
                ctx.channel().attr(ServerSession.SESSION_KEY).get();

        if (null == session || !session.isValid()) {
            log.error("session is null or isValid");
            return;
        }

        session.close();
        // 删除本地的会话和远程会话
        removeSession(session.clientId());
    }

    public void removeSession(String clientId)
    {
        if (!sessionMap.containsKey(clientId)) return;

        //本地：从会话集合中，删除会话
        sessionMap.remove(clientId);

        // TODO: 更新zk server节点，删除client
    }
}
