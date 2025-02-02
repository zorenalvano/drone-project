package com.hitachi.drone.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ErrorResponseDto {
    private String apiPath;
    private Integer errorCode;
    private String errorMessage;
    private LocalDateTime errorTime;
}
