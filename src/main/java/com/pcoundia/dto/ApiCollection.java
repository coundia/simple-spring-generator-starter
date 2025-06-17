package com.pcoundia.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonView;
import com.pcoundia.view.View;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApiCollection<T> {
    @JsonView({View.Public.class})
    private int status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
    private LocalDateTime timestamp;

    @JsonView({View.Public.class})
    private String message;

    @JsonView({View.Public.class})
    private T data;

    @JsonView({View.Public.class})
    private long totalItems;

    @JsonView({View.Public.class})
    private long totalPages;
}
