package com.example.community.dto;

import com.example.community.entity.Post;
import com.example.community.entity.User;
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

    public Post from(User user, String storedName, String originalName) {
        return Post.builder()
                .category(this.category)
                .title(this.title)
                .content(this.content)
                .user(user)
                .originalFileName(originalName)
                .storedFileName(storedName)
                .build();
    }
}
