package com.example.community.controller;

import com.example.community.entity.Post;
import com.example.community.security.CustomUserDetails;
import com.example.community.service.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;

    @GetMapping("/")
    public String index() {
        return "redirect:/home"; // 루트로 오면 홈으로 보내주는 설정이 필요할 수 있습니다.
    }

    @GetMapping("/home")
    public String home(Model model, Authentication authentication){

        List<Post> postList = homeService.getTop5Posts();
        model.addAttribute("postList", postList);

        Optional.ofNullable(authentication)
                .map(Authentication::getPrincipal)
                .filter(principal ->principal instanceof CustomUserDetails)
                .map(principal ->(CustomUserDetails)principal)
                .ifPresent(user -> model.addAttribute("username", user.getNickname()));
        return "home";
    }
}
