package com.openops.distributed;

import com.openops.cocurrent.ThreadPoolFactory;
import com.openops.common.ServerConstants;
import com.openops.common.msg.ProtoMsgFactory.ProtoMsg;
import com.openops.util.JsonUtil;
import com.openops.util.ObjectUtil;
import com.openops.zk.CuratorZKClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Slf4j
public class WorkerRouter {
    //Zk客户端
    private CuratorFramework client = null;

    private String pathRegistered = null;
    private Node node = null;

    private static WorkerRouter singleInstance = null;
    private static final String path = ServerConstants.MANAGE_PATH;

    private ConcurrentHashMap<Long, PeerSender> workerMap;

    private BiConsumer<Node, PeerSender> runAfterAdd = (node, relaySender) -> {
        doAfterAdd(node, relaySender);
    };

    private Consumer<Node> runAfterRemove = (node) -> {
        doAfterRemove(node);
    };


    public synchronized static WorkerRouter getInst() {
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

            //订阅节点的增加和删除事件
            PathChildrenCache childrenCache = new PathChildrenCache(client, path, true);
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
        Node node = ObjectUtil.JsonBytes2Object(payload, Node.class);

        long id = Worker.getWorker().getIdByPath(data.getPath());
        log.info("[TreeCache]节点删除, path={}, data={}",
                data.getPath(), JsonUtil.pojoToJson(node));


        if (runAfterRemove != null) {
            runAfterRemove.accept(node);
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
        Node node = ObjectUtil.JsonBytes2Object(payload, Node.class);

        long id = Worker.getWorker().getIdByPath(data.getPath());
        node.setId(id);

        log.info("[TreeCache]节点更新端口, path={}, data={}",
                data.getPath(), JsonUtil.pojoToJson(node));

        if (node.equals(getLocalNode())) {
            log.info("[TreeCache]本地节点, path={}, data={}",
                    data.getPath(), JsonUtil.pojoToJson(node));
            return;
        }

        PeerSender relaySender = workerMap.get(node.getId());

        //重复收到注册的事件
        if (null != relaySender && relaySender.getRmNode().equals(node)) {

            log.info("[TreeCache]节点重复增加, path={}, data={}",
                    data.getPath(), JsonUtil.pojoToJson(node));
            return;
        }

        // 建立本节点到新节点的连接
        if (runAfterAdd != null) {
            runAfterAdd.accept(node, relaySender);
        }
    }


    private void doAfterAdd(Node n, PeerSender relaySender) {
        if (null != relaySender) {
            //关闭老的连接
            relaySender.stopConnecting();
        }
        //创建一个消息转发器
        relaySender = new PeerSender(n);
        //建立转发的连接
        relaySender.doConnect();

        workerMap.put(n.getId(), relaySender);
    }


    public PeerSender route(long nodeId) {
        PeerSender peerSender = workerMap.get(nodeId);
        if (null != peerSender) {
            return peerSender;
        }
        return null;
    }


    public void sendNotification(String json) {
        workerMap.keySet().stream().forEach(
                key ->
                {
                    if (!key.equals(getLocalNode().getId())) {
                        PeerSender peerSender = workerMap.get(key);
                        ProtoMsg.Message pkg = NotificationMsgBuilder.buildNotification(json);
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
