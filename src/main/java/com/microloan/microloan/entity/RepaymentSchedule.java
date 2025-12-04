package com.microloan.microloan.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;



@Entity
@Table(name = "repayment_schedules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RepaymentSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_application_id", nullable = false)
    private LoanApplication loanApplication;
    
    @Column(nullable = false)
    private Integer installmentNumber;
    
    @Column(nullable = false)
    private Double emiAmount;
    
    @Column(nullable = false)
    private Double principalAmount;
    
    @Column(nullable = false)
    private Double interestAmount;
    
    @Column(nullable = false)
    private LocalDateTime dueDate;
    
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
    
    private LocalDateTime paidAt;
    private String transactionId;
    private String paymentMethod;
    
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        status = PaymentStatus.PENDING;
    }
}

enum PaymentStatus {
    PENDING, PAID, OVERDUE, FAILED
}

