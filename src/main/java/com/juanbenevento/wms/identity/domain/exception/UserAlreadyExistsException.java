package com.juanbenevento.wms.identity.domain.exception;

import com.juanbenevento.wms.shared.domain.exception.DomainException;

public class UserAlreadyExistsException extends DomainException {
    public UserAlreadyExistsException(String username) {
        super("El usuario ya existe: " + username);
    }
}