package com.example.community.controller;

import com.example.community.dto.Comment.CommentRequestDTO;
import com.example.community.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequestMapping("/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public String comment(@ModelAttribute CommentRequestDTO commentRequestDTO
                          , Principal principal){
        Long postId = commentRequestDTO.getPostId();
        commentService.saveComment(commentRequestDTO, principal.getName(), postId);
        return "redirect:/post/" + postId;
    }

}
