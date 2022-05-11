package com.openops.session.service;

import com.openops.common.msg.Notification;
import com.openops.distributed.Node;
import com.openops.distributed.Worker;
import com.openops.distributed.WorkerRouter;
import com.openops.session.LocalSession;
import com.openops.session.RemoteSession;
import com.openops.session.ServerSession;
import com.openops.session.dao.ClientCacheDAO;
import com.openops.session.dao.SessionCacheDAO;
import com.openops.session.entity.ClientCache;
import com.openops.session.entity.SessionCache;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class SessionManager {
    @Autowired
    ClientCacheDAO clientCacheDAO;

    @Autowired
    SessionCacheDAO sessionCacheDAO;

    private ConcurrentHashMap<String, ServerSession> sessionMap;

    private static SessionManager singleInstance = null;

    public static SessionManager getSessionManger() {
        if (singleInstance == null) {
            synchronized (SessionManager.class) {
                if (singleInstance == null) {
                    singleInstance = new SessionManager();
                }
            }
        }
        return singleInstance;
    }

    private SessionManager() {
        sessionMap = new ConcurrentHashMap<String, ServerSession>();
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
            session = new RemoteSession(sessionCache);
            sessionMap.put(sessionId, session);
        }

        return session;
    }

    public void closeLocalSession(ChannelHandlerContext ctx) {

        ServerSession session = ctx.channel().attr(LocalSession.SESSION_KEY).get();

        if (null == session || !session.isValid()) {
            log.error("session is null or isValid");
            return;
        }

        session.close();
        // 删除本地的会话和远程会话
        removeLocalSession(session.sessionId());

        Worker.getWorker().decrBalance();

        /**
         * 通知其他节点 ，用户下线
         */
        notifyOtherNodeOffLine(session);
    }

    private void notifyOtherNodeOffLine(ServerSession session) {
        if (null == session || session.isValid()) {
            log.error("session is null or isValid");
            return;
        }

        Notification<Notification.ContentWrapper> notification = Notification.wrapContent(Notification.SESSION_OFF ,session.sessionId());
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

    public void closeSession(ChannelHandlerContext ctx) {
        LocalSession localSession = LocalSession.getSession(ctx);
        localSession.close();
    }
}
