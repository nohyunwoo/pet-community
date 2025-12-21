package com.example.community.controller;

import com.example.community.entity.Post;
import com.example.community.entity.User;
import com.example.community.repository.PostLikeRepository;
import com.example.community.repository.PostRepository;
import com.example.community.repository.UserRepository;
import com.example.community.service.LikeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
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
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PostLikeRepository postLikeRepository;

    @InjectMocks
    private  LikeService likeService;

    @Test
    @DisplayName("좋아요 성공 테스트 케이스")
    void 좋아요_성공_테스트() {

        Long postId = 1L;
        Long userId = 1L;

        when(redisTemplate.hasKey(anyString())).thenReturn(false);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(anyString())).thenReturn(1L);

        when(postRepository.findById(postId)).thenReturn(Optional.of(new Post()));
        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));

        Long result = likeService.likePost(postId, userId);

        assertEquals(1L, result);
        verify(valueOperations, times(1)).increment(anyString());
    }

    @Test
    @DisplayName("좋아요 중복으로 실패 케이스")
    void 좋아요_중복_실패_테스트(){
        Long postId = 1L;
        Long userId = 1L;

        when(redisTemplate.hasKey(anyString())).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            likeService.likePost(postId, userId);
        });

        assertEquals("이미 좋아요를 누른 사용자입니다.", exception.getMessage());

        verify(valueOperations, never()).increment(anyString());
        verify(postRepository, never()).save(any());

    }

    @Test
    @DisplayName("게시글이 존재하지 않아 실패 케이스")
    void 좋아요_게시글_ID_없어_실패_테스트(){
        Long postId = 2L;
        Long userId = 1L;

        when(redisTemplate.hasKey(anyString())).thenReturn(false);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(anyString())).thenReturn(1L);

        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            likeService.likePost(postId, userId);
        });

        assertEquals("해당 게시글이 없습니다.", exception.getMessage());
        verify(userRepository, never()).findById(anyLong());
        verify(postLikeRepository, never()).save(any());
    }

    @Test
    @DisplayName("사용자이 존재하지 않아 실패 케이스")
    void 좋아요_사용자_ID_없어_실패_테스트(){
        Long postId = 2L;
        Long userId = 1L;

        when(redisTemplate.hasKey(anyString())).thenReturn(false);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(anyString())).thenReturn(1L);

        when(postRepository.findById(postId)).thenReturn(Optional.of(new Post()));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            likeService.likePost(postId, userId);
        });

        assertEquals("해당 사용자가 없습니다.", exception.getMessage());
        verify(postRepository, times(1)).findById(anyLong());
        verify(postLikeRepository, never()).save(any());
    }
}