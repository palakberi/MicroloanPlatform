package com.microloan.microloan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplicationResponse {
    private Long id;
    private Double loanAmount;
    private Integer tenureMonths;
    private Double interestRate;
    private String purpose;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;
}