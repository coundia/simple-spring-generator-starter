package com.pcoundia.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ConstraintViolationError extends ApiSubError {

    private String property;

    private Object value;

    private List<String> messages;
}
