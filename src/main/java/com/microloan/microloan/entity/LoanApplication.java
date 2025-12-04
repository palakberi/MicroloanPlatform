package com.microloan.microloan.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "loan_applications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false)
    private Double loanAmount;
    
    @Column(nullable = false)
    private Integer tenureMonths;
    
    @Column(nullable = false)
    private Double interestRate;
    
    private String purpose;
    private Double monthlyIncome;
    private Double existingDebts;
    
    @Enumerated(EnumType.STRING)
    private LoanStatus status;
    
    private String rejectionReason;
    private LocalDateTime approvedAt;
    private Long approvedBy;
    
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "loanApplication", cascade = CascadeType.ALL)
    private List<RepaymentSchedule> repaymentSchedules = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        status = LoanStatus.PENDING;
    }
}
