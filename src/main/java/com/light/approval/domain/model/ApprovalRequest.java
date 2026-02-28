package com.light.approval.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "approval_request")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ApprovalRequest {
    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;

    @ManyToOne
    @JoinColumn(name = "rule_id")
    private Rule rule;

    @ManyToOne
    @JoinColumn(name = "approver_id")
    private Approver approver;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private ApprovalStatus status;

    @ManyToOne
    @JoinColumn(name = "workflow_id", nullable = false)
    private Workflow workflow;

    public ApprovalRequest(Invoice invoice, Rule rule, Approver approver) {
        this.id = UUID.randomUUID();
        this.invoice = invoice;
        this.rule = rule;
        this.approver = approver;
        this.workflow = rule.getWorkflow();
        this.status = ApprovalStatus.PENDING;
    }
}
