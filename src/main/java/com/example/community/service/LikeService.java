package com.example.community.service;

import com.example.community.entity.Post;
import com.example.community.entity.PostLike;
import com.example.community.entity.User;
import com.example.community.exception.CustomException;
import com.example.community.exception.ErrorCode;
import com.example.community.repository.PostLikeRepository;
import com.example.community.repository.PostRepository;
import com.example.community.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LikeService {

    private final RedisTemplate<String, String> redisTemplate;
    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    public Long toggleLike(Long postId, Long userId) {
        String redisCountKey = "like_count:post:" + postId;
        String likesSetKey = "post_like:" + postId;

        Long addedCount = redisTemplate.opsForSet().add(likesSetKey, String.valueOf(userId));

        if (addedCount != null && addedCount > 0) {
            Long updatedCount = redisTemplate.opsForValue().increment(redisCountKey);
            Post post = postRepository.findById(postId).orElseThrow(() ->
                    new CustomException(ErrorCode.POST_NOT_FOUND));
            User user = userRepository.findById(userId).orElseThrow(() ->
                    new CustomException(ErrorCode.USER_NOT_FOUND));

            try{
                postLikeRepository.save(new PostLike(post, user));
            }catch(DataIntegrityViolationException e){
                redisTemplate.opsForValue().decrement(redisCountKey);
                redisTemplate.opsForSet().remove(likesSetKey, String.valueOf(userId));
                log.warn("이미 처리된 좋아요 요청입니다. userId: {}", userId);
            }
            return updatedCount;
        } else {
            Long removed = redisTemplate.opsForSet().remove(likesSetKey, String.valueOf(userId));

            if (removed != null && removed > 0) {
                Long updatedCount = redisTemplate.opsForValue().decrement(redisCountKey);
                postLikeRepository.deleteByPost_IdAndUser_Id(postId, userId);
                return updatedCount;
            }

            return redisTemplate.opsForValue().get(redisCountKey) != null ?
                    Long.parseLong(redisTemplate.opsForValue().get(redisCountKey)) : 0L;
        }
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
