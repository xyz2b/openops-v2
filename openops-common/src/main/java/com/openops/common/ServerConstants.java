package com.openops.common;

import io.netty.util.AttributeKey;

public class ServerConstants {

    //工作节点的父路径
    public static final String MANAGE_PATH = "/openops/nodes";

    //工作节点的路径前缀
    public static final String PATH_PREFIX = MANAGE_PATH + "/seq-";
    public static final String PATH_PREFIX_NO_STRIP =  "seq-";

    //统计用户数的znode
    public static final String COUNTER_PATH = "/openops/OnlineCounter";

    public static final AttributeKey<String> CHANNEL_NAME =
            AttributeKey.valueOf("CHANNEL_NAME");

}