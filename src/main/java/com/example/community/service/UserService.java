package com.example.community.service;

import com.example.community.dto.UserRequestDTO;
import com.example.community.entity.User;
import com.example.community.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public void userRegister(UserRequestDTO userRequestDTO){
        User user = User.builder()
                .displayName(userRequestDTO.getDisplayName())
                .username(userRequestDTO.getUsername())
                .password(passwordEncoder.encode(userRequestDTO.getPassword()))
                .build();
        userRepository.save(user);
    }
}
