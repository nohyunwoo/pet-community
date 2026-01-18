package com.example.community.service;

import com.example.community.dto.PostRequestDTO;
import com.example.community.entity.Post;
import com.example.community.entity.User;
import com.example.community.exception.CustomException;
import com.example.community.exception.ErrorCode;
import com.example.community.repository.PostRepository;
import com.example.community.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final FileService fileService;
    private final UserService userService;

    @Transactional
    public void savePost(PostRequestDTO dto, Long id) throws IOException {
        User user = userService.existUserId(id);

        String storedName = fileService.storeFile(dto.getImageFile());
        String originalName = (dto.getImageFile() != null) ? dto.getImageFile().getOriginalFilename() : null;

        Post post = new Post();
        post.setCategory(dto.getCategory());
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setUser(user);

        post.setOriginalFileName(originalName);
        post.setStoredFileName(storedName);

        postRepository.save(post);
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

    public Page<Post> getPosts(String keyword, Pageable pageable){
        Page<Post> posts;
        if(keyword != null && !keyword.isBlank()){
            return postRepository.searchPosts(keyword, pageable);
        }
        return postRepository.findAllWithUser(pageable);
    }
}
