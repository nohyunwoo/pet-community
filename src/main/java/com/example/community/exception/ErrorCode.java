package com.example.community.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ErrorCode {
    UNAUTHORIZED("AUTH-001", "로그인이 필요합니다.");

    private final String code;
    private final String message;

}
