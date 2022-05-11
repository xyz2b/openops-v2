package com.openops.server.openopsserver;

import com.openops.server.nettyserver.OpenopsServer;
import com.openops.server.session.service.SessionManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
//自动加载配置信息
//使包路径下带有@Value的注解自动注入
//使包路径下带有@Autowired的类可以自动注入
@ComponentScan("com.openops.server")
@SpringBootApplication
public class OpenopsServerApplication {
    public static void main(String[] args) {
        // 启动并初始化 Spring 环境及其各 Spring 组件
        ApplicationContext context = SpringApplication.run(OpenopsServerApplication.class, args);
        /**
         * 将SessionManger 单例设置为spring bean
         */
        SessionManager sessionManger = context.getBean(SessionManager.class);
        SessionManager.setSingleInstance(sessionManger);

        /**
         * 启动服务
         */
        OpenopsServer nettyServer = context.getBean(OpenopsServer.class);
        nettyServer.run();
    }

}
