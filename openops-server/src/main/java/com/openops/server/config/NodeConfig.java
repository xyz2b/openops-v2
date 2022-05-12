package com.openops.server.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
@Data
public class NodeConfig {
    @Value("${node.ip}")
    private String ip;

    @Value("${node.port}")
    private int port;
}
