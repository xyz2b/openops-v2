package com.openops.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class ClientConfig {
    @Value("${client.ip}")
    private String clientIp;

    @Value("${server.ip}")
    private String serverIp;

    @Value("${server.port}")
    private int serverPort;
}
