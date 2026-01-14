package com.example.community.controller;

import com.example.community.dto.UserRequestDTO;
import com.example.community.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/login")
    public String Login(){
        return "login";
    }

    @GetMapping("/register")
    public String register(){
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute UserRequestDTO userRequestDTO
                            , BindingResult result){
        if(result.hasErrors()){
            return "register";
        }
        userService.userRegister(userRequestDTO);
        return "redirect:/login";
    }


}
