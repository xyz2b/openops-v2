package com.openops.common.msg;

import com.openops.common.ProtoInstant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private ProtoInstant.AuthResultCode code;
    private String info;
}
