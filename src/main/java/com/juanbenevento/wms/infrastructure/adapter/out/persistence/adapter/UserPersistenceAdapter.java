package com.juanbenevento.wms.infrastructure.adapter.out.persistence.adapter;

import com.juanbenevento.wms.application.mapper.UserMapper;
import com.juanbenevento.wms.application.ports.out.UserRepositoryPort;
import com.juanbenevento.wms.domain.model.User;
import com.juanbenevento.wms.infrastructure.adapter.out.persistence.entity.UserEntity;
import com.juanbenevento.wms.infrastructure.adapter.out.persistence.repository.SpringDataUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserRepositoryPort {

    private final SpringDataUserRepository jpaRepository;
    private final UserMapper mapper;

    @Override
    public User save(User user) {
        UserEntity entity = mapper.toUserEntity(user);
        UserEntity saved = jpaRepository.save(entity);
        return mapper.toUserDomain(saved);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jpaRepository.findByUsername(username)
                .map(mapper::toUserDomain);
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpaRepository.existsByUsername(username);
    }

    @Override
    public List<User> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toUserDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return jpaRepository.existsById(id);
    }
}