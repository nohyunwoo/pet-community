package com.example.community.service;

import com.example.community.dto.CommentRequestDTO;
import com.example.community.entity.Comment;
import com.example.community.entity.Post;
import com.example.community.entity.User;
import com.example.community.exception.CustomException;
import com.example.community.exception.ErrorCode;
import com.example.community.repository.CommentRepository;
import com.example.community.repository.PostRepository;
import com.example.community.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public void saveComment(CommentRequestDTO commentRequestDTO, String username, Long postId){
        Post post = postRepository.findById(postId).orElseThrow(()
                -> new CustomException(ErrorCode.POST_NOT_FOUND));
        User user = userRepository.findByUserId(username).orElseThrow(()
                -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Comment comment = new Comment();

        comment.setContent(commentRequestDTO.getContent());
        comment.setPost(post);
        comment.setUser(user);

        try{
            commentRepository.save(comment);
        }catch(Exception e){
            log.error("댓글 저장 중 db 오류 발생: ", e);

            throw new CustomException(ErrorCode.COMMENT_SAVE_FAILED);
        }
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
