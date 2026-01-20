package com.example.community.controller;

import com.example.community.dto.Comment.CommentResponseDTO;
import com.example.community.dto.PostRequestDTO;
import com.example.community.entity.Post;
import com.example.community.security.CustomUserDetails;
import com.example.community.service.CommentService;
import com.example.community.service.LikeService;
import com.example.community.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;
    private final CommentService commentService;
    private final LikeService likeService;

    @GetMapping("/board")
    public String board(@RequestParam(required = false) String keyword,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        Model model){
        Pageable pageable = PageRequest.of
                (page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Post> posts = postService.getPosts(keyword, pageable);
        model.addAttribute("posts", posts);
        return "board";
    }

    @GetMapping("/post")
    public String post(){
        return "post";
    }

    @PostMapping("/post")
    public String savePost(@ModelAttribute PostRequestDTO postRequestDTO,
                           @AuthenticationPrincipal CustomUserDetails customUserDetails) throws IOException {
        postService.savePost(postRequestDTO, customUserDetails.getId());
        return "redirect:/board";
    }

    @PostMapping("/post-like")
    public ResponseEntity<Long> like(@RequestParam Long postId,
                                     @AuthenticationPrincipal CustomUserDetails userDetails){
        Long currentLikeCount = likeService.likePost(postId, userDetails.getId());
        return ResponseEntity.ok(currentLikeCount);
    }

    @GetMapping("/post/{id}")
    public String viewPost(@PathVariable Long id, Model model) {
        Post post = postService.findByIdAndIncreaseCount(id);
        List<CommentResponseDTO> commentByPostId = commentService.getCommentByPostId(id);
        long count = commentService.getCount(id);
        Long likeCount = likeService.getLikeCount(id);

        model.addAttribute("post", post);
        model.addAttribute("comments", commentByPostId);
        model.addAttribute("count", count);
        model.addAttribute("likeCount", likeCount);
        return "postView";
    }

    @GetMapping("/like/count")
    public ResponseEntity<Long> count(@RequestParam Long postId) {
        return ResponseEntity.ok(likeService.getLikeCount(postId));
    }
}
