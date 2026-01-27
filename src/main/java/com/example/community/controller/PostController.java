package com.example.community.controller;

import com.example.community.dto.Comment.CommentResponseDTO;
import com.example.community.dto.PostRequestDTO;
import com.example.community.entity.Post;
import com.example.community.exception.CustomException;
import com.example.community.exception.ErrorCode;
import com.example.community.repository.PostRepository;
import com.example.community.security.CustomUserDetails;
import com.example.community.service.CommentService;
import com.example.community.service.LikeService;
import com.example.community.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/post")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;
    private final PostRepository postRepository;
    private final CommentService commentService;
    private final LikeService likeService;

    @GetMapping
    public String post(){
        return "post";
    }

    @PostMapping
    public String savePost(@ModelAttribute PostRequestDTO postRequestDTO,
                           @AuthenticationPrincipal CustomUserDetails customUserDetails) throws IOException {
        postService.createPost(postRequestDTO, customUserDetails.getId());
        return "redirect:/board";
    }

    @PostMapping("/like")
    public ResponseEntity<Long> like(@RequestParam Long postId,
                                     @AuthenticationPrincipal CustomUserDetails userDetails){
        Long currentLikeCount = likeService.toggleLike(postId, userDetails.getId());
        return ResponseEntity.ok(currentLikeCount);
    }

    @GetMapping("/{id}")
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

    @GetMapping("/{id}/edit")
    public String ModifyPost(@PathVariable Long id, Model model){
        Post post = postRepository.findById(id).orElseThrow(()
                -> new CustomException(ErrorCode.POST_NOT_FOUND));
        model.addAttribute("post", post);
        return "post";
    }

    @PostMapping("/{id}/edit")
    public String saveModifyPost(@PathVariable Long id, @ModelAttribute PostRequestDTO dto){
        try{
            postService.updatePost(dto, id);
        }catch(IOException e){
            log.error("게시글 업데이트 실패: {}", e.getMessage());
        }
        return "redirect:/post/" + id;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails customUserDetails)
    {
        postService.deletePost(id, customUserDetails.getUser().getUserId());

        return ResponseEntity.ok().build();
    }


    @GetMapping("/like")
    public ResponseEntity<Long> count(@RequestParam Long postId) {
        return ResponseEntity.ok(likeService.getLikeCount(postId));
    }
}
