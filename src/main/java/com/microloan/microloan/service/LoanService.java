package com.microloan.microloan.service;

    
import com.microloan.microloan.dto.*;
import com.microloan.microloan.entity.*;
import com.microloan.microloan.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoanService {
    
    private final LoanApplicationRepository loanRepository;
    private final UserRepository userRepository;
    private final RepaymentScheduleRepository repaymentRepository;
    
    @Transactional
    public LoanApplicationResponse applyForLoan(LoanApplicationRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Calculate interest rate based on credit score
        double interestRate = calculateInterestRate(user.getCreditScore());
        
        LoanApplication loan = LoanApplication.builder()
                .user(user)
                .loanAmount(request.getLoanAmount())
                .tenureMonths(request.getTenureMonths())
                .interestRate(interestRate)
                .purpose(request.getPurpose())
                .monthlyIncome(request.getMonthlyIncome())
                .existingDebts(request.getExistingDebts())
                .status(LoanStatus.PENDING)
                .build();
        
        loan = loanRepository.save(loan);
        
        return convertToResponse(loan);
    }
    
    @Transactional
    public LoanApplicationResponse approveLoan(Long loanId, Long adminId) {
        LoanApplication loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));
        
        loan.setStatus(LoanStatus.APPROVED);
        loan.setApprovedAt(LocalDateTime.now());
        loan.setApprovedBy(adminId);
        
        // Generate repayment schedule
        generateRepaymentSchedule(loan);
        
        loan = loanRepository.save(loan);
        
        return convertToResponse(loan);
    }
    
    private void generateRepaymentSchedule(LoanApplication loan) {
        double principal = loan.getLoanAmount();
        double annualRate = loan.getInterestRate() / 100;
        double monthlyRate = annualRate / 12;
        int tenure = loan.getTenureMonths();
        
        // Calculate EMI using formula: P * r * (1+r)^n / ((1+r)^n - 1)
        double emi = (principal * monthlyRate * Math.pow(1 + monthlyRate, tenure)) 
                     / (Math.pow(1 + monthlyRate, tenure) - 1);
        
        double balance = principal;
        LocalDateTime dueDate = LocalDateTime.now().plusMonths(1);
        
        for (int i = 1; i <= tenure; i++) {
            double interestAmount = balance * monthlyRate;
            double principalAmount = emi - interestAmount;
            balance -= principalAmount;
            
            RepaymentSchedule schedule = RepaymentSchedule.builder()
                    .loanApplication(loan)
                    .installmentNumber(i)
                    .emiAmount(Math.round(emi * 100.0) / 100.0)
                    .principalAmount(Math.round(principalAmount * 100.0) / 100.0)
                    .interestAmount(Math.round(interestAmount * 100.0) / 100.0)
                    .dueDate(dueDate)
                    .status(PaymentStatus.PENDING)
                    .build();
            
            repaymentRepository.save(schedule);
            dueDate = dueDate.plusMonths(1);
        }
    }
    
    private double calculateInterestRate(Double creditScore) {
        if (creditScore >= 750) return 10.5;
        if (creditScore >= 700) return 12.0;
        if (creditScore >= 650) return 14.0;
        return 16.0;
    }
    
    public List<LoanApplicationResponse> getUserLoans(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return loanRepository.findByUserId(user.getId())
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    public List<LoanApplicationResponse> getPendingLoans() {
        return loanRepository.findByStatus(LoanStatus.PENDING)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    public List<RepaymentScheduleResponse> getRepaymentSchedule(Long loanId) {
        return repaymentRepository.findByLoanApplicationId(loanId)
                .stream()
                .map(this::convertToScheduleResponse)
                .collect(Collectors.toList());
    }
    
    private LoanApplicationResponse convertToResponse(LoanApplication loan) {
        return LoanApplicationResponse.builder()
                .id(loan.getId())
                .loanAmount(loan.getLoanAmount())
                .tenureMonths(loan.getTenureMonths())
                .interestRate(loan.getInterestRate())
                .purpose(loan.getPurpose())
                .status(loan.getStatus().name())
                .createdAt(loan.getCreatedAt())
                .approvedAt(loan.getApprovedAt())
                .build();
    }
    
    private RepaymentScheduleResponse convertToScheduleResponse(RepaymentSchedule schedule) {
        return RepaymentScheduleResponse.builder()
                .id(schedule.getId())
                .installmentNumber(schedule.getInstallmentNumber())
                .emiAmount(schedule.getEmiAmount())
                .principalAmount(schedule.getPrincipalAmount())
                .interestAmount(schedule.getInterestAmount())
                .dueDate(schedule.getDueDate())
                .status(schedule.getStatus().name())
                .paidAt(schedule.getPaidAt())
                .build();
    }
}