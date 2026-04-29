package com.example.demo.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
public enum ErrorCode {
    ERROR_CODE(1002, "User already exists"),
    UNCATEGORIZED_CODE(9999, "uncategorized error"),
    USERNAME_INVALID(1003, "username must be at least 3 characters"),
    PASSWORD_INVALID(1004, "password must be at least 8 characters"),
    KEY_VALID(1001, "enum key is invalid")
    ;

    private int code;
    private String message;

    ErrorCode (int code, String message) {
        this.code = code;
        this.message = message;
    }

}
