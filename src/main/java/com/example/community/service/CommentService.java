package com.example.community.service;

import com.example.community.dto.CommentRequestDTO;
import com.example.community.entity.Comment;
import com.example.community.entity.Post;
import com.example.community.entity.User;
import com.example.community.exception.CommentNotFoundException;
import com.example.community.repository.CommentRepository;
import com.example.community.repository.PostRepository;
import com.example.community.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public void saveComment(CommentRequestDTO commentRequestDTO, String username, Long postId){
        Post post = postRepository.findById(postId).orElseThrow(()
                -> new RuntimeException("게시글 없음"));
        User user = userRepository.findByUsername(username).orElseThrow(()
                -> new RuntimeException("사용자 없음"));

        Comment comment = new Comment();

        comment.setContent(commentRequestDTO.getContent());
        comment.setPost(post);
        comment.setUser(user);

        commentRepository.save(comment);
    }

    @Transactional
    public List<Comment> getCommentByPostId(Long id){
        List<Comment> comments = commentRepository.findAllWithUserByPostId(id);
        return comments;
    }

    public long getCount(Long id){
        long count = commentRepository.countByPostId(id);
        return count;
    }
}
