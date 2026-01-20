package com.example.community.exception;

import com.example.community.dto.ErrorResponseDTO;
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
    protected Object handleCustomException(CustomException e, HttpServletRequest request) {
        log.error("비즈니스 예외 발생: {}", e.getErrorCode().getMessage());
        return renderErrorResponse(e.getErrorCode(), request);
    }

    @ExceptionHandler(Exception.class)
    protected Object handleException(Exception e, HttpServletRequest request) {
        log.error("시스템 내부 에러 발생: ", e);
        return renderErrorResponse(ErrorCode.SERVER_ERROR, request);
    }

    private Object renderErrorResponse(ErrorCode errorCode, HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        String requestedWith = request.getHeader("X-Requested-With");

        boolean isAjax = (accept != null && accept.contains("application/json"))
                || "XMLHttpRequest".equals(requestedWith);

        if (!isAjax && accept != null && accept.contains("text/html")) {
            ModelAndView mav = new ModelAndView();
            mav.setViewName("500");
            mav.addObject("errorCode", errorCode);
            return mav;
        }

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponseDTO.from(errorCode));
    }
}