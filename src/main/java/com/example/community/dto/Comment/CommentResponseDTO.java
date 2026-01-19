package com.example.community.dto.Comment;

import com.example.community.entity.Comment;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class CommentResponseDTO {
    private Long id;
    private String username;
    private String content;
    private LocalDateTime createdAt;

    public static  CommentResponseDTO from(Comment comment){
        CommentResponseDTO dto = new CommentResponseDTO();
        dto.id = comment.getId();
        dto.username = comment.getUser().getUsername();
        dto.content = comment.getContent();
        dto.createdAt = comment.getCreatedAt();
        return dto;
    }


}
