package com.juanbenevento.wms.identity.application.port.out;

import com.juanbenevento.wms.identity.domain.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepositoryPort {
    User save(User user);
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    List<User> findAll();
    void deleteById(Long id);
    boolean existsById(Long id);
}