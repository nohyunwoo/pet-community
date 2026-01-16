package com.example.community.controller;

import com.example.community.entity.Profile;
import com.example.community.entity.User;
import com.example.community.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ProfileController {


    @GetMapping("/profile")
    public String showProfile(@AuthenticationPrincipal CustomUserDetails customUserDetails
                            , Model model){

        User loggedUser = customUserDetails.getUser();
        Profile loggedUserProfile = loggedUser.getProfile();

        model.addAttribute("user", loggedUser);
        model.addAttribute("profile", loggedUserProfile);

        return "profile";
    }
}
