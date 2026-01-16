package com.example.community.security;

import com.example.community.entity.User;
import org.springframework.security.core.GrantedAuthority;


import java.util.Collection;

public class CustomUserDetails extends org.springframework.security.core.userdetails.User {
    private final String nickname;
    private final Long id;
    private final User user;

    public CustomUserDetails(User user, Collection<? extends GrantedAuthority> authorities) {
        super(user.getUserId(), user.getPassword(), authorities);
        this.nickname = user.getUsername();
        this.id = user.getId();
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public String getNickname() {
        return nickname;
    }

    public Long getId() {
        return id;
    }
}
