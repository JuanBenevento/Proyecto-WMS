package com.juanbenevento.wms.identity.application.mapper;

import com.juanbenevento.wms.identity.application.port.in.dto.UserResponse;
import com.juanbenevento.wms.identity.domain.model.User;
import com.juanbenevento.wms.identity.infrastructure.out.persistence.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toUserResponse(User user) {
        if (user == null) return null;
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                (user.getRole() != null) ? user.getRole().name() : "N/A",
                user.getTenantId()
        );
    }

    public UserEntity toUserEntity(User domain) {
        if (domain == null) return null;
        return UserEntity.builder()
                .id(domain.getId())
                .username(domain.getUsername())
                .password(domain.getPassword())
                .role(domain.getRole())
                .tenantId(domain.getTenantId())
                .build();
    }

    public User toUserDomain(UserEntity entity) {
        if (entity == null) return null;
        return new User(
                entity.getId(),
                entity.getUsername(),
                entity.getPassword(),
                entity.getRole(),
                entity.getTenantId()
        );
    }
}