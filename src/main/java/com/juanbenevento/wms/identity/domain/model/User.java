package com.juanbenevento.wms.identity.domain.model;

import com.juanbenevento.wms.shared.domain.exception.DomainException;
import lombok.Getter;

@Getter
public class User {
    private final Long id;
    private final String username;
    private String password;
    private Role role;
    private final String tenantId;

    public User(Long id, String username, String password, Role role, String tenantId) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.tenantId = tenantId;
        validateState();
    }

    public static User create(String username, String encodedPassword, Role role, String tenantId) {
        return new User(null, username, encodedPassword, role, tenantId);
    }

    public void changePassword(String newEncodedPassword) {
        if (newEncodedPassword == null || newEncodedPassword.isBlank()) {
            throw new DomainException("La contraseña no puede estar vacía");
        }
        this.password = newEncodedPassword;
    }

    private void validateState() {
        if (username == null || username.isBlank()) throw new DomainException("El usuario es obligatorio");
        if (password == null || password.isBlank()) throw new DomainException("La contraseña es obligatoria");
        if (tenantId == null || tenantId.isBlank()) throw new DomainException("El usuario debe pertenecer a una empresa");
    }
}