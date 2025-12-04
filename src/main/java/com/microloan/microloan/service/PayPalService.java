package com.microloan.microloan.service;


import com.microloan.microloan.dto.PaymentRequest;
import com.microloan.microloan.dto.PaymentResponse;
import com.microloan.microloan.entity.*;
import com.microloan.microloan.repository.*;
import com.paypal.api.payments.*;
import com.paypal.base.rest.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayPalService {
    
    @Value("${paypal.mode}")
    private String mode;
    
    @Value("${paypal.client.id}")
    private String clientId;
    
    @Value("${paypal.client.secret}")
    private String clientSecret;
    
    private final RepaymentScheduleRepository repaymentRepository;
    
    private APIContext getAPIContext() {
        return new APIContext(clientId, clientSecret, mode);
    }
    
    public PaymentResponse createPayment(PaymentRequest request) throws Exception {
        RepaymentSchedule schedule = repaymentRepository.findById(request.getScheduleId())
                .orElseThrow(() -> new RuntimeException("Schedule not found"));
        
        Amount amount = new Amount();
        amount.setCurrency("USD");
        amount.setTotal(String.format("%.2f", schedule.getEmiAmount() / 80)); // Convert INR to USD
        
        Transaction transaction = new Transaction();
        transaction.setDescription("Loan EMI Payment - Installment #" + schedule.getInstallmentNumber());
        transaction.setAmount(amount);
        
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transaction);
        
        Payer payer = new Payer();
        payer.setPaymentMethod("paypal");
        
        Payment payment = new Payment();
        payment.setIntent("sale");
        payment.setPayer(payer);
        payment.setTransactions(transactions);
        
        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl("http://localhost:3000/payment/cancel");
        redirectUrls.setReturnUrl("http://localhost:3000/payment/success?scheduleId=" + request.getScheduleId());
        payment.setRedirectUrls(redirectUrls);
        
        Payment createdPayment = payment.create(getAPIContext());
        
        String approvalUrl = createdPayment.getLinks().stream()
                .filter(link -> link.getRel().equals("approval_url"))
                .findFirst()
                .map(Links::getHref)
                .orElse(null);
        
        return PaymentResponse.builder()
                .paymentId(createdPayment.getId())
                .status(createdPayment.getState())
                .approvalUrl(approvalUrl)
                .build();
    }
    
    @Transactional
    public PaymentResponse executePayment(String paymentId, String payerId, Long scheduleId) throws Exception {
        Payment payment = new Payment();
        payment.setId(paymentId);
        
        PaymentExecution paymentExecute = new PaymentExecution();
        paymentExecute.setPayerId(payerId);
        
        Payment executedPayment = payment.execute(getAPIContext(), paymentExecute);
        
        if (executedPayment.getState().equals("approved")) {
            RepaymentSchedule schedule = repaymentRepository.findById(scheduleId)
                    .orElseThrow(() -> new RuntimeException("Schedule not found"));
            
            schedule.setStatus(PaymentStatus.PAID);
            schedule.setPaidAt(LocalDateTime.now());
            schedule.setTransactionId(paymentId);
            schedule.setPaymentMethod("PayPal");
            
            repaymentRepository.save(schedule);
            
            return PaymentResponse.builder()
                    .paymentId(paymentId)
                    .status("SUCCESS")
                    .message("Payment completed successfully")
                    .build();
        }
        
        return PaymentResponse.builder()
                .paymentId(paymentId)
                .status("FAILED")
                .message("Payment failed")
                .build();
    }
}
