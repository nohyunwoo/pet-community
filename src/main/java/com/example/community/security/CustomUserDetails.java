package com.example.community.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class CustomUserDetails extends User {
    public String nickname;
    public Long id;

    public CustomUserDetails(String userId, String password,
                             Collection<? extends GrantedAuthority> authorities,
                             String nickname, Long id) {
        super(userId, password, authorities);
        this.nickname = nickname;
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public Long getId() {
        return id;
    }
}
