package com.juanbenevento.wms.identity.application.port.in.usescases;

public interface LoginUseCase {
    AuthResponse login(LoginCommand command);

    record LoginCommand(String username, String password) {}
    record AuthResponse(String token) {}
}