package com.openops.common.msg;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TerminalOutput {
    private int code;
    private String stdout;
    private String stderr;
    private String extraInfo;
}
