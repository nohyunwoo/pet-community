package com.example.community.dto;

import com.example.community.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ErrorResponseDTO {
    private String code;
    private String message;

    public static ErrorResponseDTO from(ErrorCode errorCode) {
        return new ErrorResponseDTO(
                errorCode.getCode(),
                errorCode.getMessage()
        );
    }

}
