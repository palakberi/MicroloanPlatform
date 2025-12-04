package com.microloan.microloan.service;


import com.microloan.microloan.entity.*;
import com.microloan.microloan.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentReminderService {
    
    private final RepaymentScheduleRepository repaymentRepository;
    private final PaymentReminderRepository reminderRepository;
    private final JavaMailSender mailSender;
    
    // Run every day at 9 AM
    @Scheduled(cron = "0 0 9 * * *")
    @Transactional
    public void sendUpcomingPaymentReminders() {
        log.info("Starting scheduled payment reminders...");
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threeDaysLater = now.plusDays(3);
        
        List<RepaymentSchedule> upcomingPayments = repaymentRepository
                .findUpcomingPayments(now, threeDaysLater);
        
        for (RepaymentSchedule schedule : upcomingPayments) {
            sendReminderEmail(schedule, "UPCOMING");
        }
        
        log.info("Sent {} upcoming payment reminders", upcomingPayments.size());
    }
    
    // Run every day at 10 AM
    @Scheduled(cron = "0 0 10 * * *")
    @Transactional
    public void sendOverduePaymentReminders() {
        log.info("Starting overdue payment reminders...");
        
        List<RepaymentSchedule> overduePayments = repaymentRepository
                .findOverduePayments(LocalDateTime.now());
        
        for (RepaymentSchedule schedule : overduePayments) {
            if (schedule.getStatus() == PaymentStatus.PENDING) {
                schedule.setStatus(PaymentStatus.OVERDUE);
                repaymentRepository.save(schedule);
                sendReminderEmail(schedule, "OVERDUE");
            }
        }
        
        log.info("Sent {} overdue payment reminders", overduePayments.size());
    }
    
    private void sendReminderEmail(RepaymentSchedule schedule, String type) {
        try {
            User user = schedule.getLoanApplication().getUser();
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject(type.equals("OVERDUE") 
                ? "⚠️ Overdue Payment Notice" 
                : "📅 Upcoming Payment Reminder");
            
            String emailBody = buildEmailBody(schedule, user, type);
            message.setText(emailBody);
            
            mailSender.send(message);
            
            // Save reminder record
            PaymentReminder reminder = PaymentReminder.builder()
                    .repaymentSchedule(schedule)
                    .sentAt(LocalDateTime.now())
                    .reminderType("EMAIL")
                    .isDelivered(true)
                    .recipientEmail(user.getEmail())
                    .build();
            
            reminderRepository.save(reminder);
            
            log.info("Sent {} reminder to {}", type, user.getEmail());
            
        } catch (Exception e) {
            log.error("Failed to send reminder email: {}", e.getMessage());
        }
    }
    
    private String buildEmailBody(RepaymentSchedule schedule, User user, String type) {
        LoanApplication loan = schedule.getLoanApplication();
        
        StringBuilder body = new StringBuilder();
        body.append("Dear ").append(user.getFullName()).append(",\n\n");
        
        if (type.equals("OVERDUE")) {
            body.append("This is to remind you that your EMI payment is OVERDUE.\n\n");
        } else {
            body.append("This is a friendly reminder about your upcoming EMI payment.\n\n");
        }
        
        body.append("Payment Details:\n");
        body.append("-------------------\n");
        body.append("Loan ID: ").append(loan.getId()).append("\n");
        body.append("Installment #: ").append(schedule.getInstallmentNumber()).append("\n");
        body.append("EMI Amount: ₹").append(String.format("%.2f", schedule.getEmiAmount())).append("\n");
        body.append("Due Date: ").append(schedule.getDueDate().toLocalDate()).append("\n");
        body.append("Status: ").append(schedule.getStatus()).append("\n\n");
        
        body.append("Please ensure timely payment to maintain a good credit score.\n\n");
        body.append("To make a payment, please log in to your account at: http://localhost:3000/dashboard\n\n");
        body.append("If you have already made the payment, please ignore this reminder.\n\n");
        body.append("Best regards,\n");
        body.append("Microloan Platform Team\n");
        body.append("support@microloan.com");
        
        return body.toString();
    }
}