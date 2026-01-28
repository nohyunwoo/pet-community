package com.example.community.service;

import com.example.community.dto.PostRequestDTO;
import com.example.community.entity.Post;
import com.example.community.entity.User;
import com.example.community.exception.CustomException;
import com.example.community.exception.ErrorCode;
import com.example.community.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final FileService fileService;
    private final UserService userService;
    private final RedisTemplate<String, String> redisTemplate;

    @Transactional
    public void createPost(PostRequestDTO dto, Long id) throws IOException{
        User user = userService.existUserId(id);

        String storedName = fileService.storeFile(dto.getImageFile());
        String originalName = (dto.getImageFile() != null) ? dto.getImageFile().getOriginalFilename() : null;
        Post post = dto.from(user, storedName, originalName);
        postRepository.save(post);
    }

    @Transactional
    public void updatePost(PostRequestDTO dto, Long id) throws IOException{
        Post post = postRepository.findById(id).orElseThrow(() ->
                new CustomException(ErrorCode.POST_NOT_FOUND));

        if(dto.getImageFile() != null && !dto.getImageFile().isEmpty()){
            if(post.getStoredFileName() != null){
                fileService.deleteFile(post.getStoredFileName());
            }
            String storedName = fileService.storeFile(dto.getImageFile());
            post.setStoredFileName(storedName);
            post.setOriginalFileName(dto.getImageFile().getOriginalFilename());
        }

        post.update(dto.getTitle(), dto.getContent(), dto.getCategory());
    }

    @Transactional
    public void deletePost(Long id, String userId){
        Post post = postRepository.findById(id).orElseThrow(()
                -> new CustomException(ErrorCode.POST_NOT_FOUND));
        String storedFileName = post.getStoredFileName();

        if(!post.getUser().getUserId().equals(userId)){
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        // Redis 삭제
        redisTemplate.delete("like_count:post:" + id);
        redisTemplate.delete("post_like:" + id);

        // 사진 삭제
        if (storedFileName != null) {
            deletePhysicalFile(storedFileName);
        }

        postRepository.delete(post);
    }

    @Value("${file.upload-path}")
    private String uploadPath;

    public void deletePhysicalFile(String fileName) {
        Path filePath = Paths.get(uploadPath).resolve(fileName);

        try {
            Files.deleteIfExists(filePath);
            log.info("파일 삭제 성공: {}", filePath.toAbsolutePath());
        } catch (IOException e) {
            log.error("파일 삭제 실패: {}", e.getMessage());
        }
    }

    @Transactional
    public Post findByIdAndIncreaseCount(Long id){
        Optional<Post> optionPost = postRepository.findById(id);

        if(optionPost.isEmpty()){
            throw new CustomException(ErrorCode.POST_NOT_FOUND);
        }

        Post post = optionPost.get();
        post.increaseCount();
        return post;
    }
}
