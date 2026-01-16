package com.example.community.repository;

import com.example.community.entity.Post;
import com.example.community.entity.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
    @Query("SELECT p FROM Post p WHERE p.user.userId = :userId")
    Page<Post> findByUserId(String userId, Pageable pageable);
}
