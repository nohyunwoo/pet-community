package com.example.community.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LikeRequestDTO {
    private Long postId;
    private Long userId;
}
