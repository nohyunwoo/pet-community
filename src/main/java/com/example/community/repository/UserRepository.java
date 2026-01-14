package com.example.community.repository;

import com.example.community.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
    Optional<User> findByUserId(String userId);
    Optional<User> findById(Long id);
    boolean existsByUserId(String userId);
}
