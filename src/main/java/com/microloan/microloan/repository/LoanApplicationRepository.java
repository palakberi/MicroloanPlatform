package com.microloan.microloan.repository;

import com.microloan.microloan.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;


@Repository
public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {
    List<LoanApplication> findByUserId(Long userId);
    List<LoanApplication> findByStatus(LoanStatus status);
    
    @Query("SELECT l FROM LoanApplication l WHERE l.status = :status ORDER BY l.createdAt ASC")
    List<LoanApplication> findPendingApplications(LoanStatus status);
}
