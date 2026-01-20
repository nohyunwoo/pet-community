package com.example.community.service;

import com.example.community.entity.Post;
import com.example.community.entity.Post_like;
import com.example.community.entity.User;
import com.example.community.exception.CustomException;
import com.example.community.exception.ErrorCode;
import com.example.community.repository.PostLikeRepository;
import com.example.community.repository.PostRepository;
import com.example.community.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final RedisTemplate<String, String> redisTemplate;
    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public Long likePost(Long postId, Long userId) {
        String redisCountKey = "like_count:post:" + postId;
        String userLikeKey = "post_like:" + postId + ":" + userId;

        if (Boolean.TRUE.equals(redisTemplate.hasKey(userLikeKey))) {
            throw new CustomException(ErrorCode.LIKE_SAME);
        }

        Long UpdatedCount = redisTemplate.opsForValue().increment(redisCountKey);

        redisTemplate.opsForValue().set(userLikeKey, "true");

        Post post = postRepository.findById(postId).orElseThrow(() ->
                new CustomException(ErrorCode.POST_NOT_FOUND));
        User user = userRepository.findById(userId).orElseThrow(() ->
                new CustomException(ErrorCode.USER_NOT_FOUND));

        Post_like postLike = new Post_like(post, user);

        postLikeRepository.save(postLike);
        return UpdatedCount;
    }

    public Long getLikeCount(Long postId) {
        String redisKey = "like_count:post:" + postId;

        return Optional.ofNullable(redisTemplate.opsForValue().get(redisKey))
                .map(Long::parseLong)
                .orElseGet(() -> {
                    if(!postRepository.existsById(postId)){
                        throw new CustomException(ErrorCode.POST_NOT_FOUND);
                    }

                    Long count = postLikeRepository.countByPostId(postId);
                    redisTemplate.opsForValue().set(redisKey, String.valueOf(count));
                    return count;
                });
    }
}
