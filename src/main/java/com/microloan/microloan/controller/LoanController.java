package com.microloan.microloan.controller;


import com.microloan.microloan.dto.*;
import com.microloan.microloan.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {
    
    private final LoanService loanService;
    
    @PostMapping("/apply")
    public ResponseEntity<LoanApplicationResponse> applyForLoan(
            @RequestBody LoanApplicationRequest request,
            Authentication authentication) {
        String userEmail = authentication.getName();
        return ResponseEntity.ok(loanService.applyForLoan(request, userEmail));
    }
    
    @GetMapping("/my-loans")
    public ResponseEntity<List<LoanApplicationResponse>> getMyLoans(Authentication authentication) {
        String userEmail = authentication.getName();
        return ResponseEntity.ok(loanService.getUserLoans(userEmail));
    }
    
    @GetMapping("/{loanId}/schedule")
    public ResponseEntity<List<RepaymentScheduleResponse>> getRepaymentSchedule(
            @PathVariable Long loanId) {
        return ResponseEntity.ok(loanService.getRepaymentSchedule(loanId));
    }
}