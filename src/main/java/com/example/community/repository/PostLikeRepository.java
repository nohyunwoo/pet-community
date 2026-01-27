package com.example.community.repository;

import com.example.community.entity.Post;
import com.example.community.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    Long countByPostId(Long postId);

    Optional<PostLike> findByPost(Post post);

    @Modifying
    void deleteByPost_IdAndUser_Id(Long postId, Long userId);
}
