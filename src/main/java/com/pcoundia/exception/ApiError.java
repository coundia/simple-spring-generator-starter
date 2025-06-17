package com.pcoundia.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import javax.validation.ConstraintViolation;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@JsonTypeInfo(include = JsonTypeInfo.As.WRAPPER_OBJECT, use = JsonTypeInfo.Id.NONE  , property = "error", visible = true)
public class ApiError {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
    private LocalDateTime timestamp;
    private HttpStatus status;
    private String message;
    private String debugMessage;

    private List<ApiSubError> violations;


    private ApiError() {
        timestamp = LocalDateTime.now();
    }

    ApiError(HttpStatus status) {
        this();
        this.status = status;
    }

    ApiError(HttpStatus status, Throwable ex) {
        this();
        this.status = status;
        this.message = "Unexpected error";
        this.debugMessage = ex.getLocalizedMessage();
    }

    ApiError(HttpStatus status, String message, Throwable ex) {
        this();
        this.status = status;
        this.message = message;
        this.debugMessage = ex.getLocalizedMessage();
    }

    private void addSubError(ApiSubError subError) {
        if (violations == null) {
            violations = new ArrayList<ApiSubError>();
        }
        boolean subErrorExist = false;
        int i = 0;
        // Si les subError sont des violationError, alors on verifie si la propriete de l'erreur existe deja.
        // Si c'est le cas, on ajoute le message de la nouvelle erreur dans la list sinon, on ajoute la nouvelle erreur
        for (ApiSubError violationError: violations) {
            if (subError instanceof ConstraintViolationError && violationError instanceof ConstraintViolationError) {
                if (((ConstraintViolationError) violationError).getProperty().equals(((ConstraintViolationError) subError).getProperty())) {
                    subErrorExist = true;


                    List<String> message1 = (((ConstraintViolationError) violations.get(i)).getMessages());

                    List<String> message2 = ((ConstraintViolationError) subError).getMessages();

                    List<String> combinedList = Stream.of(message1, message2)
                            .flatMap(x -> x.stream())
                            .collect(Collectors.toList());
                    ((ConstraintViolationError) violations.get(i)).setMessages(combinedList);

                    // ((ConstraintViolationError) violations.get(i)).getMessages().addAll((((ConstraintViolationError) subError).getMessages()));
                }
            }
            i++;
        }
        if (!subErrorExist)
            violations.add(subError);

    }

    private void addValidationError(String field, Object rejectedValue, String message) {
        addSubError(new ConstraintViolationError(field, rejectedValue, Collections.singletonList(message)));
    }

    private void addValidationError(String field, String message) {
        addSubError(new ConstraintViolationError(field,null ,  Collections.singletonList(message)));
    }

    private void addValidationError(FieldError fieldError) {
        this.addValidationError(
                fieldError.getField(),
                fieldError.getRejectedValue(),
                fieldError.getDefaultMessage());
    }

    public void addValidationErrors(List<FieldError> fieldErrors) {
        fieldErrors.forEach(this::addValidationError);
    }

    private void addValidationError(ObjectError objectError) {
        this.addValidationError(
                objectError.getObjectName(),
                objectError.getDefaultMessage());
    }

    public void addValidationError(List<ObjectError> globalErrors) {
        globalErrors.forEach(this::addValidationError);
    }

    /**
     * Utility method for adding error of ConstraintViolation. Usually when a @Validated validation fails.
     *
     * @param cv the ConstraintViolation
     */
    private void addValidationError(ConstraintViolation<?> cv) {
        this.addValidationError(
                // cv.getRootBeanClass().getSimpleName(),
                ((PathImpl) cv.getPropertyPath()).getLeafNode().asString(),
                cv.getInvalidValue(),
                cv.getMessage());
    }

    public void addValidationErrors(Set<ConstraintViolation<?>> constraintViolations) {
        constraintViolations.forEach(this::addValidationError);
    }

    public void addViolationErrors(List<ApiSubError> violations) {
        violations.forEach(this::addSubError);
    }

}
