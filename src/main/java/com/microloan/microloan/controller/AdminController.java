package com.microloan.microloan.controller;


import com.microloan.microloan.dto.*;
import com.microloan.microloan.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    
    private final LoanService loanService;
    
    @GetMapping("/pending-loans")
    public ResponseEntity<List<LoanApplicationResponse>> getPendingLoans() {
        return ResponseEntity.ok(loanService.getPendingLoans());
    }
    
    @PutMapping("/loans/{loanId}/approve")
    public ResponseEntity<LoanApplicationResponse> approveLoan(
            @PathVariable Long loanId,
            @RequestParam Long adminId) {
        return ResponseEntity.ok(loanService.approveLoan(loanId, adminId));
    }
}