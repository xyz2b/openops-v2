package com.openops.server.session.service;

import com.openops.server.distributed.Node;
import com.openops.server.distributed.Worker;
import com.openops.server.distributed.WorkerRouter;
import com.openops.server.session.LocalSession;
import com.openops.server.session.RemoteSession;
import com.openops.server.session.ServerSession;
import com.openops.server.session.dao.ClientCacheDAO;
import com.openops.server.session.dao.SessionCacheDAO;
import com.openops.server.session.entity.ClientCache;
import com.openops.server.session.entity.SessionCache;
import com.openops.common.msg.Notification;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service("SessionManager")
public class SessionManager {
    @Autowired
    ClientCacheDAO clientCacheDAO;

    @Autowired
    SessionCacheDAO sessionCacheDAO;

    private ConcurrentHashMap<String, ServerSession> sessionMap = new ConcurrentHashMap<String, ServerSession>();;

    private static SessionManager singleInstance = null;

    public static SessionManager getSessionManger() {
        return singleInstance;
    }

    public static void setSingleInstance(SessionManager singleInstance) {
        SessionManager.singleInstance = singleInstance;
    }

    public void addLocalSession(ServerSession session) {
        // 保存本地session到本地会话缓存
        String sessionId = session.sessionId();
        sessionMap.put(sessionId, session);

        // 保存session到redis
        String clientId = session.clientId();
        Node node = Worker.getWorker().getLocalNodeInfo();
        SessionCache sessionCache = new SessionCache(sessionId, clientId, node);
        sessionCacheDAO.save(sessionCache);

        // 保存用户信息到redis
        clientCacheDAO.save(new ClientCache(clientId, sessionCache));

        Worker.getWorker().incBalance();

        notifyOtherNodeOnLine(sessionCache);

    }

    private void notifyOtherNodeOnLine(SessionCache session) {
        Notification<SessionCache> notification = new Notification<SessionCache>(Notification.SESSION_ON , session);
        WorkerRouter.getWorkerRouter().sendNotification(notification);
    }

    public ServerSession getLocalSession(String sessionId) {
        ServerSession serverSession = sessionMap.get(sessionId);
        if (null == serverSession) {
            log.info("client：{} 下线了? 没有任何会话 ", sessionId);
            return null;
        }
        return serverSession;
    }

    public ServerSession getSessionByClientId(String clientId) {
        // 只有LocalSession才会缓存到redis中
        // redis中缓存了所有节点的LocalSession
        // redis中缓存了，Client、Node、Session的关系，通过ClientId就能查到该Client的连接属于哪个Node，以及该Client连接所对应的SessionID
        // LocalSession: 本节点所管理的Client的Session信息
        ClientCache clientCache = clientCacheDAO.get(clientId);
        if (clientCache == null) {
            log.info("用户：{} 下线了? 没有在缓存中找到记录 ", clientId);
            return null;
        }

        SessionCache sessionCache = clientCache.getSessionCache();
        String sessionId = sessionCache.getSessionId();
        // 在本地，取得session
        ServerSession session = sessionMap.get(sessionId);
        // 本地没有，创建远程的session，加入会话集合
        if (session == null) {
            // RemoteSession不会写入redis，只在节点本地存着
            // key为该ClientID对应的Client所属节点的Session的ID(ClientId对应的Client所连接的节点生成的Session的ID)
            // value为RemoteSession，RemoteSession中存储这该ClientID对应的Client所属节点的Session信息(ClientId对应的Client所连接的节点生成的Session)
            // 先通过ClientId从redis中查到该Client所连接的节点的SessionID，然后再在本节点的缓存中通过SessionID就能查到该Client属于哪个Node(sessionCache: Session、Client和Node的关系)，从而进行消息转发
            // 每个节点为其所管理的每个Client生成的SessionId全局唯一，当Client连接的不是本节点时，它的Session信息是它所连接的节点生成的，所以需要通过RemoteSession来标识不在本节点所管理的Client的Session信息
            // RemoteSession: 不在本节点所管理的Client的Session信息
            session = new RemoteSession(sessionCache);
            sessionMap.put(sessionId, session);
        }

        return session;
    }

    public List<ServerSession> getSessionByClientIds(List<String> clientIds) {
        // 只有LocalSession才会缓存到redis中
        // redis中缓存了所有节点的LocalSession
        // redis中缓存了，Client、Node、Session的关系，通过ClientId就能查到该Client的连接属于哪个Node，以及该Client连接所对应的SessionID
        // LocalSession: 本节点所管理的Client的Session信息
        List<ClientCache> clientCaches = clientCacheDAO.multiGet(clientIds);

        List<ServerSession> sessions = new ArrayList<>(clientCaches.size());
        for(ClientCache clientCache : clientCaches) {
            SessionCache sessionCache = clientCache.getSessionCache();
            String sessionId = sessionCache.getSessionId();
            // 在本地，取得session
            ServerSession session = sessionMap.get(sessionId);
            // 本地没有，创建远程的session，加入会话集合
            if (session == null) {
                // RemoteSession不会写入redis，只在节点本地存着
                // key为该ClientID对应的Client所属节点的Session的ID(ClientId对应的Client所连接的节点生成的Session的ID)
                // value为RemoteSession，RemoteSession中存储这该ClientID对应的Client所属节点的Session信息(ClientId对应的Client所连接的节点生成的Session)
                // 先通过ClientId从redis中查到该Client所连接的节点的SessionID，然后再在本节点的缓存中通过SessionID就能查到该Client属于哪个Node(sessionCache: Session、Client和Node的关系)，从而进行消息转发
                // 每个节点为其所管理的每个Client生成的SessionId全局唯一，当Client连接的不是本节点时，它的Session信息是它所连接的节点生成的，所以需要通过RemoteSession来标识不在本节点所管理的Client的Session信息
                // RemoteSession: 不在本节点所管理的Client的Session信息
                session = new RemoteSession(sessionCache);
                sessionMap.put(sessionId, session);
            }
            sessions.add(session);
        }

        return sessions;
    }

    public void closeSession(ChannelHandlerContext ctx) {

        ServerSession session = ctx.channel().attr(LocalSession.SESSION_KEY).get();

        if (null == session || !session.isValid()) {
            log.error("session is null or isValid");
            return;
        }

        SessionCache sessionCache = sessionCacheDAO.get(session.sessionId());

        session.close();
        // 删除本地会话
        removeLocalSession(session.sessionId());

        Worker.getWorker().decrBalance();

        /**
         * 通知其他节点 ，用户下线
         */
        notifyOtherNodeOffLine(sessionCache);
    }

    private void notifyOtherNodeOffLine(SessionCache session) {
        if (null == session) {
            log.error("session is null or isValid");
            return;
        }

        Notification<SessionCache> notification = new Notification<SessionCache>(Notification.SESSION_OFF , session);
        WorkerRouter.getWorkerRouter().sendNotification(notification);
    }

    public void removeLocalSession(String sessionId) {
        if (!sessionMap.containsKey(sessionId)) return;

        ServerSession session = sessionMap.get(sessionId);
        String clientId = session.clientId();

        log.info("本地session减少：{} 下线了", clientId);

        // 删除clientCache
        clientCacheDAO.remove(clientId);

        // 删除SessionCache
        sessionCacheDAO.remove(sessionId);

        //本地：从会话集合中，删除会话
        sessionMap.remove(sessionId);
    }

    public void removeRemoteSession(String sessionId) {
        if (!sessionMap.containsKey(sessionId)) {
            return;
        }
        sessionMap.remove(sessionId);
    }

    public void addRemoteSession(SessionCache sessionCache) {
        String sessionId = sessionCache.getSessionId();
        if (!sessionMap.containsKey(sessionId)) {
            ServerSession session = new RemoteSession(sessionCache);
            sessionMap.put(sessionId, session);
        }
    }
}
