package com.example.community.controller;

import com.example.community.entity.Post;
import com.example.community.entity.Profile;
import com.example.community.entity.User;
import com.example.community.repository.ProfileRepository;
import com.example.community.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileRepository profileRepository;

    @GetMapping
    public String showProfile(@AuthenticationPrincipal CustomUserDetails customUserDetails
                            , Model model
                            , @PageableDefault(size = 5, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable){

        User loggedUser = customUserDetails.getUser();
        Profile loggedUserProfile = loggedUser.getProfile();
        Page<Post> savedMyPosts = profileRepository.findByUserId(loggedUser.getUserId(), pageable);

        model.addAttribute("user", loggedUser);
        model.addAttribute("profile", loggedUserProfile);
        model.addAttribute("posts", savedMyPosts);

        return "profile";
    }
}
