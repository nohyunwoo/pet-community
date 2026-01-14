package com.example.community.dto;

import com.example.community.entity.Post;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostRequestDTO {
    private String category;
    private String title;
    private String content;

    private MultipartFile imageFile;

    public PostRequestDTO(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
