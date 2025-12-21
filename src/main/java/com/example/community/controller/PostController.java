package com.example.community.controller;

import com.example.community.dto.LikeRequestDTO;
import com.example.community.dto.PostRequestDTO;
import com.example.community.entity.Comment;
import com.example.community.entity.Post;
import com.example.community.entity.Post_like;
import com.example.community.entity.User;
import com.example.community.repository.CommentRepository;
import com.example.community.repository.PostLikeRepository;
import com.example.community.repository.PostRepository;
import com.example.community.repository.UserRepository;
import com.example.community.security.CustomUserDetails;
import com.example.community.service.CommentService;
import com.example.community.service.LikeService;
import com.example.community.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

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
                           Principal principal){
        postService.savePost(postRequestDTO, principal.getName());
        return "redirect:/board";
    }

    @GetMapping("/post/{id}")
    public String viewPost(@PathVariable Long id, Model model) {
        Post post = postService.findByIdAndIncreaseCount(id); // 조회 + 증가
        List<Comment> commentByPostId = commentService.getCommentByPostId(id);
        long count = commentService.getCount(id);
        Long likeCount = likeService.getLikeCount(id);

        model.addAttribute("post", post);
        model.addAttribute("comments", commentByPostId);
        model.addAttribute("count", count);
        model.addAttribute("likeCount", likeCount);
        return "postView";
    }

    @PostMapping("/post/like")
    public ResponseEntity<Long> like(@RequestParam Long postId,
                                     @AuthenticationPrincipal CustomUserDetails userDetails){
        Long currentLikeCount = likeService.likePost(postId, userDetails.getId());
        return ResponseEntity.ok(currentLikeCount);
    }

    @GetMapping("/like/count")
    public ResponseEntity<Long> count(@RequestParam Long postId) {
        return ResponseEntity.ok(likeService.getLikeCount(postId));
    }
}
