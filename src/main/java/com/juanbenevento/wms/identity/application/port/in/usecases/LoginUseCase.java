package com.juanbenevento.wms.identity.application.port.in.usecases;

public interface LoginUseCase {
    AuthResponse login(LoginCommand command);

    record LoginCommand(String username, String password) {}
    record AuthResponse(String token) {}
}