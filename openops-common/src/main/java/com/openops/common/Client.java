package com.openops.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Client {
    private String clientId;
    private String clientVersion;
    private String token;
    private String platform;
}
