package com.light.approval.domain.repository;

import com.light.approval.domain.model.ApprovalRequest;
import com.light.approval.domain.model.ApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ApprovalRequestRepository extends JpaRepository<ApprovalRequest, UUID> {
    boolean existsByInvoiceIdAndStatusIn(UUID invoiceId, List<ApprovalStatus> statuses);
}
