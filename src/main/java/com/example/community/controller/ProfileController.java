package com.example.community.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class ProfileController {


    @GetMapping("/profile")
    public String showProfile(){

        return "profile.html";
    }
}
