package com.tanmay.advance_Calculator_Backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CalculationResponse {
    private Long id;
    private String expression;
    private String result;
    private LocalDateTime timestamp;
}
