package com.openops.server.config;

import com.openops.util.SpringContextUtil;
import com.openops.zk.CuratorZKClient;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class ZkClientConfig implements ApplicationContextAware {
    @Value("${zookeeper.connect.url}")
    private String zkConnect;

    @Value("${zookeeper.connect.SessionTimeout}")
    private String zkSessionTimeout;

    /**
     * @see BeanInitializationException
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContextUtil.setContext(applicationContext);
    }

    // spring装配初始化ZK时，会调用该方法创建ZKClient实例
    @Bean(name = "curatorZKClient")
    public CuratorZKClient curatorZKClient() {
        return new CuratorZKClient(zkConnect, zkSessionTimeout);
    }
}