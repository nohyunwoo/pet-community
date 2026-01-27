package com.example.community.service;

import com.example.community.entity.Post;
import com.example.community.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BoardService {
    private final PostRepository postRepository;

    public Page<Post> getPosts(String keyword, Pageable pageable){
        Page<Post> posts;
        if(keyword != null && !keyword.isBlank()){
            return postRepository.searchPosts(keyword, pageable);
        }
        return postRepository.findAllWithUser(pageable);
    }
}
