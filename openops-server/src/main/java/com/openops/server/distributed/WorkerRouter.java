package com.openops.server.distributed;

import com.openops.server.builder.NotificationMsgBuilder;
import com.openops.util.JsonUtil;
import com.openops.util.ObjectUtil;
import com.openops.zk.CuratorZKClient;
import com.openops.cocurrent.ThreadPoolFactory;
import com.openops.common.ServerConstants;
import com.openops.common.msg.Notification;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * 工作节点管理
 * 通过监听ZK中节点信息的增删，从而管理与其他节点的连接，用于消息转发
 * */
@Slf4j
public class WorkerRouter {
    //Zk客户端
    private CuratorFramework client;

    // 单例
    private static WorkerRouter singleInstance;

    // 管理本节点与其他所有节点的连接
    private ConcurrentHashMap<Long, PeerSender> workerMap;

    // 新增节点的回调处理函数
    private BiConsumer<Node, PeerSender> runAfterAdd = (node, relaySender) -> {
        doAfterAdd(node, relaySender);
    };

    // 删除节点的回调处理函数
    private Consumer<Node> runAfterRemove = (node) -> {
        doAfterRemove(node);
    };

    public synchronized static WorkerRouter getWorkerRouter() {
        if (null == singleInstance) {
            synchronized (WorkerRouter.class) {
                if (null == singleInstance) {
                    singleInstance = new WorkerRouter();
                }
            }
        }
        return singleInstance;
    }

    private WorkerRouter() {
        workerMap = new ConcurrentHashMap<>();
    }

    private boolean init = false;

    /**
     * 初始化节点管理
     */
    public void init() {

        if(init) {
            return;
        }
        init = true;
        try {
            if (null == client) {
                this.client = CuratorZKClient.instance.getClient();

            }

            // 订阅节点的增加和删除事件
            PathChildrenCache childrenCache = new PathChildrenCache(client, ServerConstants.MANAGE_PATH, true);
            PathChildrenCacheListener childrenCacheListener = new PathChildrenCacheListener() {
                @Override
                public void childEvent(CuratorFramework client,
                                       PathChildrenCacheEvent event) throws Exception {
                    log.info("开始监听其他的Worker子节点:-----");
                    ChildData data = event.getData();
                    switch (event.getType()) {
                        case CHILD_ADDED:
                            log.info("CHILD_ADDED : " + data.getPath() + "  数据:" + data.getData());
                            processNodeAdded(data);
                            break;
                        case CHILD_REMOVED:
                            log.info("CHILD_REMOVED : " + data.getPath() + "  数据:" + data.getData());
                            processNodeRemoved(data);
                            break;
                        case CHILD_UPDATED:
                            log.info("CHILD_UPDATED : " + data.getPath() + "  数据:" + new String(data.getData()));
                            break;
                        default:
                            log.debug("[PathChildrenCache]节点数据为空, path={}", data == null ? "null" : data.getPath());
                            break;
                    }
                }

            };

            childrenCache.getListenable().addListener(
                    childrenCacheListener, ThreadPoolFactory.getIoIntenseTargetThreadPool());
            System.out.println("Register zk watcher successfully!");
            childrenCache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 节点删除的处理
     *
     * @param data 被删除的节点
     * */
    private void processNodeRemoved(ChildData data) {
        byte[] payload = data.getData();
        Node remoteNode = ObjectUtil.JsonBytes2Object(payload, Node.class);

        log.info("[TreeCache]节点删除, path={}, data={}", data.getPath(), JsonUtil.pojoToJson(remoteNode));

        if (runAfterRemove != null) {
            runAfterRemove.accept(remoteNode);
        }
    }

    private void doAfterRemove(Node node) {
        PeerSender peerSender = workerMap.get(node.getId());

        if (null != peerSender) {
            peerSender.stopConnecting();
            workerMap.remove(node.getId());
        }
    }

    /**
     * 节点增加的处理
     *
     * @param data 新节点
     */
    private void processNodeAdded(ChildData data) {
        byte[] payload = data.getData();
        Node remoteNode = ObjectUtil.JsonBytes2Object(payload, Node.class);

        long id = Worker.getWorker().getIdByPath(data.getPath());
        remoteNode.setId(id);

        log.info("[TreeCache]节点更新端口, path={}, data={}",
                data.getPath(), JsonUtil.pojoToJson(remoteNode));

        if (remoteNode.equals(getLocalNode())) {
            log.info("[TreeCache]本地节点, path={}, data={}",
                    data.getPath(), JsonUtil.pojoToJson(remoteNode));
            return;
        }

        PeerSender relaySender = workerMap.get(remoteNode.getId());

        // 重复收到注册的事件
        if (null != relaySender && relaySender.getRmNode().equals(remoteNode)) {

            log.info("[TreeCache]节点重复增加, path={}, data={}",
                    data.getPath(), JsonUtil.pojoToJson(remoteNode));
            return;
        }

        // 建立本节点到新节点的连接
        if (runAfterAdd != null) {
            runAfterAdd.accept(remoteNode, relaySender);
        }
    }


    private void doAfterAdd(Node remoteNode, PeerSender relaySender) {
        if (null != relaySender) {
            // 关闭老的连接
            relaySender.stopConnecting();
        }

        // 创建一个消息转发器
        relaySender = new PeerSender(remoteNode);
        // 建立转发的连接
        relaySender.doConnect();

        workerMap.put(remoteNode.getId(), relaySender);
    }

    public PeerSender route(Node node) {
        return workerMap.get(node.getId());
    }

    // 给其他节点发送通知
    public void sendNotification(Notification notification) {
        workerMap.keySet().stream().forEach(
                key ->
                {
                    if (!key.equals(getLocalNode().getId())) {
                        Node node = Worker.getWorker().getLocalNodeInfo();
                        PeerSender peerSender = workerMap.get(key);
                        Object pkg = new NotificationMsgBuilder(peerSender.getClientSession().sessionId(), notification).build();
                        peerSender.writeAndFlush(pkg);
                    }
                }
        );
    }


    public Node getLocalNode() {
        return Worker.getWorker().getLocalNodeInfo();
    }

    public void remove(Node remoteNode) {
        workerMap.remove(remoteNode.getId());
        log.info("[TreeCache]移除远程节点信息,  node={}", JsonUtil.pojoToJson(remoteNode));
    }
}
