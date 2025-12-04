package com.microloan.microloan.repository;

import com.microloan.microloan.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface RepaymentScheduleRepository extends JpaRepository<RepaymentSchedule, Long> {
    List<RepaymentSchedule> findByLoanApplicationId(Long loanApplicationId);
    
    @Query("SELECT r FROM RepaymentSchedule r WHERE r.status = 'PENDING' AND r.dueDate BETWEEN :start AND :end")
    List<RepaymentSchedule> findUpcomingPayments(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT r FROM RepaymentSchedule r WHERE r.status = 'PENDING' AND r.dueDate < :now")
    List<RepaymentSchedule> findOverduePayments(LocalDateTime now);
}