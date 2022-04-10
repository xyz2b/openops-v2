package com.openops.common.msg;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TerminalCommand {
    private String cmd;
    private String user;
    private int timeout;
    private int priority;
}
