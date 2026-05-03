package com.example.demo.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    USER_EXISTED(1002, "User already exists", HttpStatus.BAD_REQUEST),
    UNCATEGORIZED_CODE(9999, "uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    USERNAME_INVALID(1003, "username must be at least 3 characters", HttpStatus.INTERNAL_SERVER_ERROR),
    PASSWORD_INVALID(1004, "password must be at least 8 characters", HttpStatus.INTERNAL_SERVER_ERROR),
    KEY_INVALID(1001, "enum key is invalid", HttpStatus.INTERNAL_SERVER_ERROR),
    USER_NOT_EXISTED(1005, "User not exists", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1006, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "You do not have permission", HttpStatus.FORBIDDEN),
    ;

    private int code;
    private String message;
    private HttpStatusCode statusCode;
    ErrorCode (int code, String message,  HttpStatusCode httpStatusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = httpStatusCode;
    }

}
