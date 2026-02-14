package com.example.community.service;

import com.example.community.dto.PostRequestDTO;
import com.example.community.entity.Post;
import com.example.community.entity.User;
import com.example.community.exception.CustomException;
import com.example.community.exception.ErrorCode;
import com.example.community.repository.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private FileService fileService;

    @Mock
    private UserService userService;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @InjectMocks
    private PostService postService;

    @Test
    @DisplayName("게시글 생성 성공 - 이미지 포함")
    void createPost_success_withImage() throws IOException {
        // given
        Long userId = 1L;
        User user = createUser(1L, "user01");
        MultipartFile imageFile = mockImageFile("cat.png", false);
        PostRequestDTO dto = new PostRequestDTO("FREE", "title", "content", imageFile);
        when(userService.existUserId(userId)).thenReturn(user);
        when(fileService.storeFile(imageFile)).thenReturn("stored.jpg");

        // when
        postService.createPost(dto, userId);

        // then
        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(captor.capture());
        Post saved = captor.getValue();
        assertEquals("FREE", saved.getCategory());
        assertEquals("title", saved.getTitle());
        assertEquals("content", saved.getContent());
        assertEquals("stored.jpg", saved.getStoredFileName());
        assertEquals("cat.png", saved.getOriginalFileName());
        assertEquals(user, saved.getUser());
    }

    @Test
    @DisplayName("게시글 생성 성공 - 이미지 없음")
    void createPost_success_withoutImage() throws IOException {
        // given
        Long userId = 1L;
        User user = createUser(1L, "user01");
        PostRequestDTO dto = new PostRequestDTO("QNA", "title", "content", null);
        when(userService.existUserId(userId)).thenReturn(user);
        when(fileService.storeFile(null)).thenReturn(null);

        // when
        postService.createPost(dto, userId);

        // then
        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(captor.capture());
        Post saved = captor.getValue();
        assertEquals("QNA", saved.getCategory());
        assertEquals(null, saved.getStoredFileName());
        assertEquals(null, saved.getOriginalFileName());
    }

    @Test
    @DisplayName("게시글 생성 실패 - DTO null")
    void createPost_nullDto_throwsNullPointerException() {
        // given
        Long userId = 1L;
        when(userService.existUserId(userId)).thenReturn(createUser(1L, "user01"));

        // when
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> postService.createPost(null, userId));

        // then
        assertNotNull(exception);
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    @DisplayName("게시글 생성 실패 - 사용자 조회 CustomException 전파")
    void createPost_userServiceThrowsCustomException_propagates() {
        // given
        Long userId = 10L;
        PostRequestDTO dto = new PostRequestDTO("FREE", "title", "content", null);
        when(userService.existUserId(userId)).thenThrow(new CustomException(ErrorCode.USER_NOT_FOUND));

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> postService.createPost(dto, userId));

        // then
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    @DisplayName("게시글 생성 실패 - 파일 저장 IOException 전파")
    void createPost_storeFileThrowsIOException_propagates() throws IOException {
        // given
        Long userId = 1L;
        User user = createUser(1L, "user01");
        MultipartFile imageFile = mockImageFile("bad.png", false);
        PostRequestDTO dto = new PostRequestDTO("FREE", "title", "content", imageFile);
        when(userService.existUserId(userId)).thenReturn(user);
        when(fileService.storeFile(imageFile)).thenThrow(new IOException("disk error"));

        // when
        IOException exception = assertThrows(IOException.class,
                () -> postService.createPost(dto, userId));

        // then
        assertEquals("disk error", exception.getMessage());
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    @DisplayName("게시글 생성 실패 - DB 저장 에러 전파")
    void createPost_saveThrowsRuntimeException_propagates() throws IOException {
        // given
        Long userId = 1L;
        User user = createUser(1L, "user01");
        PostRequestDTO dto = new PostRequestDTO("FREE", "title", "content", null);
        when(userService.existUserId(userId)).thenReturn(user);
        when(fileService.storeFile(null)).thenReturn(null);
        doThrow(new RuntimeException("DB error")).when(postRepository).save(any(Post.class));

        // when
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> postService.createPost(dto, userId));

        // then
        assertEquals("DB error", exception.getMessage());
    }

    @Test
    @DisplayName("게시글 수정 성공 - 이미지 교체")
    void updatePost_success_withImageReplace() throws IOException {
        // given
        Long postId = 1L;
        MultipartFile newImage = mockImageFile("new.png", false);
        PostRequestDTO dto = new PostRequestDTO("NOTICE", "new title", "new content", newImage);
        Post post = createPost(postId, createUser(1L, "author01"));
        post.setStoredFileName("old.jpg");
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(fileService.storeFile(newImage)).thenReturn("new.jpg");

        // when
        postService.updatePost(dto, postId);

        // then
        verify(fileService).deleteFile("old.jpg");
        verify(fileService).storeFile(newImage);
        assertEquals("NOTICE", post.getCategory());
        assertEquals("new title", post.getTitle());
        assertEquals("new content", post.getContent());
        assertEquals("new.jpg", post.getStoredFileName());
        assertEquals("new.png", post.getOriginalFileName());
    }

    @Test
    @DisplayName("게시글 수정 성공 - 이미지 미교체")
    void updatePost_success_withoutImageReplace() throws IOException {
        // given
        Long postId = 2L;
        MultipartFile emptyImage = mockImageFile("empty.png", true);
        PostRequestDTO dto = new PostRequestDTO("QNA", "updated", "updated content", emptyImage);
        Post post = createPost(postId, createUser(1L, "author01"));
        post.setStoredFileName("keep.jpg");
        post.setOriginalFileName("keep.png");
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        // when
        postService.updatePost(dto, postId);

        // then
        verify(fileService, never()).deleteFile(anyString());
        verify(fileService, never()).storeFile(any(MultipartFile.class));
        assertEquals("QNA", post.getCategory());
        assertEquals("updated", post.getTitle());
        assertEquals("updated content", post.getContent());
        assertEquals("keep.jpg", post.getStoredFileName());
    }

    @Test
    @DisplayName("게시글 수정 실패 - 게시글 없음")
    void updatePost_notFound_throwsCustomException() {
        // given
        Long postId = 99L;
        PostRequestDTO dto = new PostRequestDTO("FREE", "title", "content", null);
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> postService.updatePost(dto, postId));

        // then
        assertEquals(ErrorCode.POST_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("게시글 수정 실패 - 파일 삭제 에러 전파")
    void updatePost_deleteFileThrowsRuntimeException_propagates() {
        // given
        Long postId = 1L;
        MultipartFile newImage = mockImageFile("new.png", false);
        PostRequestDTO dto = new PostRequestDTO("NOTICE", "title", "content", newImage);
        Post post = createPost(postId, createUser(1L, "author01"));
        post.setStoredFileName("old.jpg");
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        doThrow(new RuntimeException("delete error")).when(fileService).deleteFile("old.jpg");

        // when
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> postService.updatePost(dto, postId));

        // then
        assertEquals("delete error", exception.getMessage());
    }

    @Test
    @DisplayName("게시글 수정 실패 - 파일 저장 CustomException 전파")
    void updatePost_storeFileThrowsCustomException_propagates() throws IOException {
        // given
        Long postId = 1L;
        MultipartFile newImage = mockImageFile("new.png", false);
        PostRequestDTO dto = new PostRequestDTO("NOTICE", "title", "content", newImage);
        Post post = createPost(postId, createUser(1L, "author01"));
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(fileService.storeFile(newImage)).thenThrow(new CustomException(ErrorCode.IMAGES_PROCESS_ERROR));

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> postService.updatePost(dto, postId));

        // then
        assertEquals(ErrorCode.IMAGES_PROCESS_ERROR, exception.getErrorCode());
    }

    @Test
    @DisplayName("게시글 삭제 성공 - 파일 존재")
    void deletePost_success_withFile() {
        // given
        Long postId = 1L;
        String userId = "author01";
        Post post = createPost(postId, createUser(1L, userId));
        post.setStoredFileName("delete.jpg");
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(redisTemplate.delete("like_count:post:" + postId)).thenReturn(true);
        when(redisTemplate.delete("post_like:" + postId)).thenReturn(true);

        // when
        postService.deletePost(postId, userId);

        // then
        verify(redisTemplate).delete("like_count:post:" + postId);
        verify(redisTemplate).delete("post_like:" + postId);
        verify(fileService).deleteFile("delete.jpg");
        verify(postRepository).delete(post);
    }

    @Test
    @DisplayName("게시글 삭제 성공 - 파일 없음")
    void deletePost_success_withoutFile() {
        // given
        Long postId = 1L;
        String userId = "author01";
        Post post = createPost(postId, createUser(1L, userId));
        post.setStoredFileName(null);
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        // when
        postService.deletePost(postId, userId);

        // then
        verify(fileService, never()).deleteFile(anyString());
        verify(postRepository).delete(post);
    }

    @Test
    @DisplayName("게시글 삭제 실패 - 게시글 없음")
    void deletePost_notFound_throwsCustomException() {
        // given
        Long postId = 99L;
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> postService.deletePost(postId, "author01"));

        // then
        assertEquals(ErrorCode.POST_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("게시글 삭제 실패 - 권한 없음")
    void deletePost_forbidden_throwsCustomException() {
        // given
        Long postId = 1L;
        Post post = createPost(postId, createUser(1L, "author01"));
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> postService.deletePost(postId, "other-user"));

        // then
        assertEquals(ErrorCode.FORBIDDEN, exception.getErrorCode());
        verify(redisTemplate, never()).delete(anyString());
        verify(fileService, never()).deleteFile(anyString());
        verify(postRepository, never()).delete(any(Post.class));
    }

    @Test
    @DisplayName("게시글 삭제 실패 - userId null(권한 없음)")
    void deletePost_nullUserId_throwsCustomException() {
        // given
        Long postId = 1L;
        Post post = createPost(postId, createUser(1L, "author01"));
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> postService.deletePost(postId, null));

        // then
        assertEquals(ErrorCode.FORBIDDEN, exception.getErrorCode());
    }

    @Test
    @DisplayName("게시글 삭제 실패 - Redis 에러 전파")
    void deletePost_redisDeleteThrowsRuntimeException_propagates() {
        // given
        Long postId = 1L;
        String userId = "author01";
        Post post = createPost(postId, createUser(1L, userId));
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(redisTemplate.delete("like_count:post:" + postId))
                .thenThrow(new RuntimeException("redis error"));

        // when
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> postService.deletePost(postId, userId));

        // then
        assertEquals("redis error", exception.getMessage());
        verify(postRepository, never()).delete(any(Post.class));
    }

    @Test
    @DisplayName("게시글 조회 및 조회수 증가 성공")
    void findByIdAndIncreaseCount_success() {
        // given
        Long postId = 1L;
        Post post = createPost(postId, createUser(1L, "author01"));
        post.setCount(3L);
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        // when
        Post result = postService.findByIdAndIncreaseCount(postId);

        // then
        assertEquals(post, result);
        assertEquals(4L, result.getCount());
    }

    @Test
    @DisplayName("게시글 조회 및 조회수 증가 실패 - 게시글 없음")
    void findByIdAndIncreaseCount_notFound_throwsCustomException() {
        // given
        Long postId = 123L;
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> postService.findByIdAndIncreaseCount(postId));

        // then
        assertEquals(ErrorCode.POST_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("게시글 조회 및 조회수 증가 실패 - null id")
    void findByIdAndIncreaseCount_nullId_throwsIllegalArgumentException() {
        // given
        when(postRepository.findById(isNull()))
                .thenThrow(new IllegalArgumentException("The given id must not be null"));

        // when
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> postService.findByIdAndIncreaseCount(null));

        // then
        assertEquals("The given id must not be null", exception.getMessage());
    }

    @Test
    @DisplayName("게시글 조회 및 조회수 증가 실패 - DB 에러 전파")
    void findByIdAndIncreaseCount_dbError_propagates() {
        // given
        Long postId = 1L;
        when(postRepository.findById(anyLong())).thenThrow(new RuntimeException("DB read error"));

        // when
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> postService.findByIdAndIncreaseCount(postId));

        // then
        assertEquals("DB read error", exception.getMessage());
    }

    private User createUser(Long id, String userId) {
        return User.builder()
                .id(id)
                .username("tester")
                .userId(userId)
                .password("encoded")
                .sex("M")
                .build();
    }

    private Post createPost(Long id, User user) {
        return Post.builder()
                .id(id)
                .category("FREE")
                .title("title")
                .content("content")
                .user(user)
                .count(0L)
                .build();
    }

    private MultipartFile mockImageFile(String originalFilename, boolean empty) {
        MultipartFile imageFile = org.mockito.Mockito.mock(MultipartFile.class);
        lenient().when(imageFile.isEmpty()).thenReturn(empty);
        lenient().when(imageFile.getOriginalFilename()).thenReturn(originalFilename);
        return imageFile;
    }
}
