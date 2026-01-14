package com.example.community.exception;

import com.example.community.dto.ErrorResponseDTO; // 패키지 경로 확인!
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    protected ResponseEntity<ErrorResponseDTO> handleCustomException(CustomException e) {
        log.error("서버 내부 에러 발생: ", e);

        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponseDTO.from(errorCode));
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponseDTO> handleException(Exception e) {
        log.error("서버 내부 에러 발생: ", e);

        return ResponseEntity
                .status(500)
                .body(new ErrorResponseDTO("COMMON-001", "서버 내부 에러가 발생했습니다."));
    }
}