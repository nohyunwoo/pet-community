package com.example.community.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentRequestDTO {
    private String content;
    private Long postId;

    public CommentRequestDTO(String content, Long postId) {
        this.content = content;
        this.postId = postId;
    }
}

