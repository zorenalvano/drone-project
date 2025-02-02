package com.hitachi.drone.dto;

import lombok.*;

@Data
@AllArgsConstructor
public class ResponseDto {
    private Integer statusCode;
    private String statusMsg;
}
