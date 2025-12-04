package com.microloan.microloan.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_reminders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentReminder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repayment_schedule_id")
    private RepaymentSchedule repaymentSchedule;
    
    private LocalDateTime sentAt;
    private String reminderType; // EMAIL, SMS
    private Boolean isDelivered;
    private String recipientEmail;
}