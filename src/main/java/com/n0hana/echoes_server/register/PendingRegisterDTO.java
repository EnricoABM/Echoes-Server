package com.n0hana.echoes_server.register;

import com.n0hana.echoes_server.user.UserRole;

public record PendingRegisterDTO(
    String name,

    String email,

    String password,

    UserRole role
) {
    
}
