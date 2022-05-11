package com.openops.client.openopsclient;

import com.openops.client.nettyclient.OpenopsClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("com.openops.client")
@SpringBootApplication
public class OpenopsClientApplication {

    public static void main(String[] args) {
        // 启动并初始化 Spring 环境及其各 Spring 组件
        ApplicationContext context = SpringApplication.run(OpenopsClientApplication.class, args);

        OpenopsClient nettClient = context.getBean(OpenopsClient.class);
        nettClient.doConnect();
    }
}
