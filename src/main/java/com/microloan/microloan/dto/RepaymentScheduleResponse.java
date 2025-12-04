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
public class RepaymentScheduleResponse {
    private Long id;
    private Integer installmentNumber;
    private Double emiAmount;
    private Double principalAmount;
    private Double interestAmount;
    private LocalDateTime dueDate;
    private String status;
    private LocalDateTime paidAt;
}