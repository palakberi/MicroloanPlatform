package com.microloan.microloan.controller;


import com.microloan.microloan.dto.*;
import com.microloan.microloan.service.PayPalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    
    private final PayPalService payPalService;
    
    @PostMapping("/create")
    public ResponseEntity<PaymentResponse> createPayment(@RequestBody PaymentRequest request) {
        try {
            return ResponseEntity.ok(payPalService.createPayment(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(PaymentResponse.builder()
                            .status("ERROR")
                            .message(e.getMessage())
                            .build());
        }
    }
    
    @GetMapping("/execute")
    public ResponseEntity<PaymentResponse> executePayment(
            @RequestParam String paymentId,
            @RequestParam String PayerID,
            @RequestParam Long scheduleId) {
        try {
            return ResponseEntity.ok(payPalService.executePayment(paymentId, PayerID, scheduleId));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(PaymentResponse.builder()
                            .status("ERROR")
                            .message(e.getMessage())
                            .build());
        }
    }
}
