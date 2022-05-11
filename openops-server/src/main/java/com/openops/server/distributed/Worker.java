package com.openops.server.distributed;

import com.openops.util.JsonUtil;
import com.openops.zk.CuratorZKClient;
import com.openops.common.ServerConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

import java.util.List;

/**
 * 工作节点
 * 当前节点上线之后，保存当前节点的信息，同时将自己注册到ZK中
 * */
@Slf4j
public class Worker {
    // Zk curator 客户端
    private CuratorFramework client;

    // 保存当前Znode节点的路径，在zk中创建后返回
    private String pathRegistered;

    // 当前节点的信息，如节点的地址和端口等
    private Node localNode;

    // 单例模式
    private static Worker singleInstance;

    // 是否初始化过，在节点上线时，会像zk注册本节点，将本节点的信息写入zk对应Znode路径处
    private boolean init = false;

    // 获取单例
    public static Worker getWorker() {
        if (null == singleInstance) {
            synchronized (Worker.class) {
                if (null == singleInstance) {
                    singleInstance = new Worker();
                }
            }
        }
        return singleInstance;
    }

    private Worker() {
        localNode = new Node();
    }

    // 在zookeeper中创建临时节点，服务注册
    public synchronized void init() {
        if (init) {
            return;
        }
        init = true;
        if (null == client) {
            this.client = CuratorZKClient.instance.getClient();
        }
        if (null == localNode) {
            localNode = new Node();
        }

        // 如果 zk 注册路径 中没有节点，就删除该路径
        deleteWhenHasNoChildren(ServerConstants.MANAGE_PATH);

        // 如果当前 zk 中没有注册路径，就创建
        createParentIfNeeded(ServerConstants.MANAGE_PATH);

        // 创建一个 临时顺序 ZNode 节点, 节点的 payload 为当前Node信息
        try {
            byte[] payload = JsonUtil.object2JsonBytes(localNode);

            pathRegistered = client.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                    .forPath(ServerConstants.PATH_PREFIX, payload);

            // 从zk自动生成的顺序节点名中获取ID信息，同时为 node 设置id
            localNode.setId(getId());
            payload = JsonUtil.object2JsonBytes(localNode);
            client.setData().forPath(pathRegistered, payload);

            log.info("本地节点, path={}, id={}", pathRegistered, localNode.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setLocalNode(String ip, int port) {
        localNode.setHost(ip);
        localNode.setPort(port);
    }

    /**
     * 不存在子节点时，删除父节点
     *
     * @param path 父节点路径
     * */
    private void deleteWhenHasNoChildren(String path) {

        int index = path.lastIndexOf("/");

        String parent = path.substring(0, index);

        boolean exist = isNodeExist(parent);
        if (exist) {
            List<String> children = getChildren(parent);
            if (null != children && children.size() == 0) {
                delPath(parent);
                log.info("删除空的 父节点:" + parent);
            }
        }
    }

    /**
     * 父节点不存在时，创建父节点
     *
     * @param managePath 父节点路径
     */
    private void createParentIfNeeded(String managePath) {

        try {
            Stat stat = client.checkExists().forPath(managePath);
            if (null == stat) {
                client.create()
                        .creatingParentsIfNeeded()
                        .withProtection()
                        .withMode(CreateMode.PERSISTENT)
                        .forPath(managePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 取得节点编号，从ZK自动生成的路径中获取
     *
     * @return 编号
     */
    public long getId() {
        return getIdByPath(pathRegistered);
    }

    /**
     * 返回本地的节点信息
     *
     * @return 本地的节点信息
     */
    public Node getLocalNodeInfo() {
        return localNode;
    }

    /**
     * 取得节点编号
     *
     * @param path 路径
     * @return 编号
     */
    public long getIdByPath(String path) {
        String sid = null;
        if (null == path) {
            throw new RuntimeException("节点路径有误");
        }
        int index = path.lastIndexOf(ServerConstants.PATH_PREFIX);
        if (index >= 0) {
            index += ServerConstants.PATH_PREFIX.length();
            sid = index <= path.length() ? path.substring(index) : null;
        }

        if (null == sid) {
            throw new RuntimeException("节点ID获取失败");
        }

        return Long.parseLong(sid);
    }

    /**
     * 增加负载，表示有客户端登录成功
     *
     * @return 成功状态
     */
    public boolean incBalance() {
        if (null == localNode) {
            throw new RuntimeException("还没有设置Node 节点");
        }
        // 增加负载：增加负载，并写回zookeeper
        while (true) {
            try {
                localNode.incrementBalance();
                byte[] payload = JsonUtil.object2JsonBytes(localNode);
                client.setData().forPath(pathRegistered, payload);
                return true;
            } catch (Exception e) {
                return false;
            }
        }

    }

    /**
     * 减少负载，表示有客户端下线，写回zookeeper
     *
     * @return 成功状态
     */
    public boolean decrBalance() {
        if (null == localNode) {
            throw new RuntimeException("还没有设置Node 节点");
        }
        while (true) {
            try {

                localNode.decrementBalance();

                byte[] payload = JsonUtil.object2JsonBytes(localNode);
                client.setData().forPath(pathRegistered, payload);
                return true;
            } catch (Exception e) {
                return false;
            }
        }

    }

    private boolean isNodeExist(String zkPath) {
        try {
            Stat stat = client.checkExists().forPath(zkPath);
            if (null == stat) {
                log.info("节点不存在:", zkPath);
                return false;
            } else {
                log.info("节点存在 stat is:", stat.toString());
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // 删除该路径
    public boolean delPath(String path) {
        boolean b = false;

        //检测是否存在该路径。
        try {
            Void stat = client.delete().forPath(path);
            b = stat == null ? false : true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return b;
    }

    // 获取子节点
    public List<String> getChildren(String path) {
        //检测是否存在该路径。
        try {
            List<String> children = client.getChildren().forPath(path);
            return children;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
