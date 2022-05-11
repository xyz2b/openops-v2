package com.openops.server.distributed;

import lombok.Data;
import java.io.Serializable;
import java.util.Objects;

/**
 * 当前节点的信息
 * */
@Data
public class Node implements Comparable<Node>, Serializable {
    private static final long serialVersionUID = -499010884211304846L;

    // server node Id, zookeeper负责生成
    private long id;

    // Netty server node 的连接数
    private Integer balance = 0;

    // Netty server node IP
    private String host;

    // Netty server node 端口
    private Integer port;

    public Node() {

    }

    public Node(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public String toString() {
        return "Node{" +
                "id='" + id + '\'' +
                "host='" + host + '\'' +
                ", port='" + port + '\'' +
                ",balance=" + balance +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return Objects.equals(host, node.host) &&
                Objects.equals(port, node.port);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id, host, port);
    }

    @Override
    public int compareTo(Node o) {
        int weight1 = this.balance;
        int weight2 = o.balance;
        if (weight1 > weight2)
        {
            return 1;
        } else if (weight1 < weight2)
        {
            return -1;
        }
        return 0;
    }

    public void incrementBalance()
    {
        balance++;
    }

    public void decrementBalance()
    {
        balance--;
    }
}