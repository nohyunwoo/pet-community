package com.example.community.controller;

import com.example.community.dto.CommentRequestDTO;
import com.example.community.entity.Comment;
import com.example.community.entity.Post;
import com.example.community.entity.User;
import com.example.community.repository.CommentRepository;
import com.example.community.repository.PostRepository;
import com.example.community.repository.UserRepository;
import com.example.community.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/comment")
    public String comment(@ModelAttribute CommentRequestDTO commentRequestDTO
                          , Principal principal){
        Long postId = commentRequestDTO.getPostId();
        commentService.saveComment(commentRequestDTO, principal.getName(), postId);
        return "redirect:/post/" + postId;
    }

}
