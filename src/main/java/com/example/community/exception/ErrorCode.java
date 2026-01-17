package com.example.community.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ErrorCode {
    UNAUTHORIZED(401, "AUTH-001", "로그인이 필요합니다."),

    FORBIDDEN(403, "AUTH-002", "해당 요청에 권한이 없습니다."),
    ACCESS_DENIED(403, "AUTH-003", "접근 권한이 없습니다."),
    SERVER_ERROR(403, "AUTH-003", "접근 권한이 없습니다.");

    private final int status;
    private final String code;
    private final String message;

}
