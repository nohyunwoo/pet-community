package com.example.community.repository;

import com.example.community.entity.Post;
import com.example.community.entity.Post_like;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<Post_like, Long> {

    Long countByPostId(Long postId);
    Optional<Post_like> findByPost(Post post);
}
