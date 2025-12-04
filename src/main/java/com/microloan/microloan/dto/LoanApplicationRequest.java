package com.microloan.microloan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplicationRequest {
    private Double loanAmount;
    private Integer tenureMonths;
    private String purpose;
    private Double monthlyIncome;
    private Double existingDebts;
}