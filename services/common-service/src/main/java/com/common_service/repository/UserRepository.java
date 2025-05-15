package com.common_service.repository;

import com.common_service.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    User findByUsernameOrEmail(String username, String email);

    Optional<User> findByProviderId(String providerId);

}
