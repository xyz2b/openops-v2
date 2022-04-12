package com.openops.common.msg;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TerminalCommand {
    private String host;
    private String cmd;
    private String user;
    private int timeout;
    private int priority;
    private String uuid;
}
