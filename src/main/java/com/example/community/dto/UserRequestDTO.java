package com.example.community.dto;

import com.example.community.entity.Profile;
import com.example.community.entity.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

@Getter
@Setter
public class UserRequestDTO {
    @NotBlank(message = "이름은 필수입니다.")
    private String username;

    @NotBlank(message = "아이디는 필수입니다.")
    private String userId;

    @NotBlank(message = "비밀번호는 필수입니다.")
    private String password;

    @NotBlank(message = "비밀번호 확인은 필수입니다.")
    private String passwordConfirm;

    @NotBlank(message = "성별은 필수입니다.")
    private String sex;

    @NotNull(message = "생년월일은 필수입니다.")
    private LocalDate userDate;

    public User toEntity(PasswordEncoder passwordEncoder){
        User user = User.builder()
                        .username(this.username)
                        .userId(this.userId)
                        .password(passwordEncoder.encode(this.password))
                        .sex(this.sex)
                        .userDate(this.userDate)
                        .build();

        user.createDefaultProfile();
        return user;
    }
}
