package com.pcoundia.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiException extends RuntimeException{

    private HttpStatus status = HttpStatus.BAD_REQUEST;

    public ApiException() {
        super();
    }

    public ApiException(String message) {
        super(message);
    }

    public ApiException(String message, HttpStatus httpStatus) {
        super(message);
        this.status = httpStatus;
    }


}
