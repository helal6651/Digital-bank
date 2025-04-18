package com.bankingsystem.account_service.repository;
import com.bankingsystem.account_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
