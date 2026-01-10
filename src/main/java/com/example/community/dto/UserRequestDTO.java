package com.example.community.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRequestDTO {
    @NotBlank
    private String displayName;
    @NotBlank
    private String username;
    @NotBlank
    private String password;
}
