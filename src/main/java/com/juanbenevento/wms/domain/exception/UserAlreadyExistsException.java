package com.juanbenevento.wms.domain.exception;

public class UserAlreadyExistsException extends DomainException {
    public UserAlreadyExistsException(String username) {
        super("El usuario ya existe: " + username);
    }
}