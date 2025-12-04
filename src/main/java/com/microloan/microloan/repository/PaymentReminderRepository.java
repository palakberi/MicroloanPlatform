package com.microloan.microloan.repository;


import com.microloan.microloan.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;


@Repository
public interface PaymentReminderRepository extends JpaRepository<PaymentReminder, Long> {
    List<PaymentReminder> findByRepaymentScheduleId(Long scheduleId);
}