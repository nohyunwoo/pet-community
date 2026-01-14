package com.example.community.service;

import com.example.community.entity.Post;
import com.example.community.repository.PostRepository;
import com.example.community.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HomeService {
    private final PostRepository postRepository;

    public List<Post> getTop5Posts(){
        return postRepository.findTop5Posts(PageRequest.of(0,5));
    }
}
