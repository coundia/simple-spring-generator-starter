package com.pcoundia.exception;

import java.util.ArrayList;
import java.util.List;

public class BadRequestException extends RuntimeException{

  private final List<ApiSubError> violations;
  public BadRequestException(String message) {
    super(message);
    violations = new ArrayList<>();
  }

  public BadRequestException(String message, List<ApiSubError> violations) {
    super(message);
    this.violations = violations;
  }

  public List<ApiSubError> getViolations() {
    return violations;
  }
}
