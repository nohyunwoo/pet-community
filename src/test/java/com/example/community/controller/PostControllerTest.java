package com.example.community.controller;

import com.example.community.entity.Post;
import com.example.community.entity.PostLike;
import com.example.community.entity.User;
import com.example.community.exception.CustomException;
import com.example.community.exception.ErrorCode;
import com.example.community.repository.PostLikeRepository;
import com.example.community.repository.PostRepository;
import com.example.community.repository.UserRepository;
import com.example.community.service.LikeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostControllerTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private SetOperations<String, String> setOperations;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PostLikeRepository postLikeRepository;

    @InjectMocks
    private  LikeService likeService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
    }

    @Test
    @DisplayName("좋아요 성공 테스트 케이스")
    void 좋아요_성공_테스트() {
        // given
        Long postId = 1L;
        Long userId = 1L;
        String likesSetKey = "post_like:" + postId;

        // Set에 추가 성공 (1L 반환 -> 신규 좋아요)
        when(setOperations.add(likesSetKey, String.valueOf(userId))).thenReturn(1L);
        when(valueOperations.increment(anyString())).thenReturn(1L);
        when(postRepository.findById(postId)).thenReturn(Optional.of(new Post()));
        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));

        // when
        Long result = likeService.toggleLike(postId, userId);

        // then
        assertEquals(1L, result);
        verify(setOperations, times(1)).add(anyString(), anyString());
        verify(valueOperations, times(1)).increment(anyString());
        verify(postLikeRepository, times(1)).save(any(PostLike.class));
    }

    @Test
    @DisplayName("좋아요 취소(삭제) 테스트 케이스")
    void 좋아요_취소_테스트() {
        // given
        Long postId = 1L;
        Long userId = 1L;
        String likesSetKey = "post_like:" + postId;

        // 이미 존재하여 추가 실패 (0L 반환 -> else 블록으로 이동)
        when(setOperations.add(likesSetKey, String.valueOf(userId))).thenReturn(0L);
        // Set에서 삭제 성공 (1L 반환)
        when(setOperations.remove(likesSetKey, String.valueOf(userId))).thenReturn(1L);
        when(valueOperations.decrement(anyString())).thenReturn(0L);

        // when
        Long result = likeService.toggleLike(postId, userId);

        // then
        assertEquals(0L, result);
        verify(setOperations, times(1)).remove(anyString(), anyString());
        verify(postLikeRepository, times(1)).deleteByPost_IdAndUser_Id(postId, userId);
    }

    @Test
    @DisplayName("게시글이 존재하지 않아 실패 케이스")
    void 좋아요_게시글_ID_없어_실패_테스트() {
        // given
        Long postId = 2L;
        Long userId = 1L;

        when(setOperations.add(anyString(), anyString())).thenReturn(1L); // 신규 등록 시도
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            likeService.toggleLike(postId, userId);
        });

        // 실제 로직의 에러코드에 맞춰 수정 (ErrorCode 사용 시)
        assertEquals(ErrorCode.POST_NOT_FOUND, exception.getErrorCode());
        verify(postLikeRepository, never()).save(any());
    }

    @Test
    @DisplayName("사용자가 존재하지 않아 실패 케이스")
    void 좋아요_사용자_ID_없어_실패_테스트() {
        // given
        Long postId = 1L;
        Long userId = 99L;

        when(setOperations.add(anyString(), anyString())).thenReturn(1L);
        when(postRepository.findById(postId)).thenReturn(Optional.of(new Post()));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(CustomException.class, () -> {
            likeService.toggleLike(postId, userId);
        });

        verify(postLikeRepository, never()).save(any());
    }
}