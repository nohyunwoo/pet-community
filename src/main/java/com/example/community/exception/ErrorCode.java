package com.example.community.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ErrorCode {
    UNAUTHORIZED(401, "AUTH-001", "로그인이 필요합니다."),

    FORBIDDEN(403, "AUTH-002", "해당 요청에 권한이 없습니다."),
    ACCESS_DENIED(403, "AUTH-003", "접근 권한이 없습니다."),
    USERID_NOT_FOUND(403, "AUTH-004", "아이디가 존재하지 않습니다."),


    USER_NOT_FOUND(404, "U-001", "사용자를 찾을 수 없습니다."),

    POST_NOT_FOUND(404, "P-001", "게시글을 찾을 수 없습니다."),

    LIKE_SAME(400, "L-001", "이미 좋아요를 누른 사용자입니다."),

    IMAGES_PROCESS_ERROR(400, "I-001", "업로드한 이미지 파일이 손상되었거나 지원하지 않는 형식입니다."),
    IMAGE_SIZE_EXCEEDED(400, "I-002", "파일 크기가 너무 큽니다. (최대 5MB)"),

    COMMENT_SAVE_FAILED(500, "CM-001", "댓글 저장 중 오류가 발생했습니다."),
    SERVER_ERROR(500, "C-003", "서버 내부 에러가 발생했습니다.");

    private final int status;
    private final String code;
    private final String message;

}
