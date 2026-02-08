package com.example.community.service;

import com.example.community.dto.UserRequestDTO;
import com.example.community.entity.Profile;
import com.example.community.entity.User;
import com.example.community.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Transactional
    public void userRegister(UserRequestDTO userRequestDTO){

        if(!userRequestDTO.getPassword().equals(userRequestDTO.getPasswordConfirm())){
            throw new IllegalArgumentException("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }

        if(userRepository.existsByUserId(userRequestDTO.getUserId())){
            throw new IllegalArgumentException("이미 사용중인 아이디입니다.");
        }

        User user = userRequestDTO.toEntity(passwordEncoder);

        userRepository.save(user);
    }

    public User existUserId(Long id){
        return userRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("존재하지 않는 사용자 입니다."));
    }
}
