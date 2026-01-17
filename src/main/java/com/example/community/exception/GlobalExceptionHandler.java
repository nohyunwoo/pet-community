package com.example.community.exception;

import com.example.community.dto.ErrorResponseDTO; // 패키지 경로 확인!
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.ModelAndView;

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
    protected Object handleException(Exception e, HttpServletRequest request) {
        log.error("서버 내부 에러 발생: ", e);


        String accept = request.getHeader("Accept");

        if (accept != null && accept.contains("text/html")) {
            ModelAndView mav = new ModelAndView();
            mav.setViewName("500");
            return mav;
        }

            ErrorCode errorCode = ErrorCode.SERVER_ERROR;
            return ResponseEntity
                    .status(errorCode.getStatus())
                    .body(new ErrorResponseDTO(errorCode.getCode(), errorCode.getMessage()));
    }
}