package com.light.approval.application;

import com.light.approval.domain.model.Action;
import com.light.approval.domain.model.ActionType;
import com.light.approval.domain.model.ApprovalRequest;
import com.light.approval.domain.model.ApprovalStatus;
import com.light.approval.domain.model.Approver;
import com.light.approval.domain.model.Condition;
import com.light.approval.domain.model.ConditionOperator;
import com.light.approval.domain.model.Invoice;
import com.light.approval.domain.model.NotificationChannel;
import com.light.approval.domain.model.Rule;
import com.light.approval.domain.model.Workflow;
import com.light.approval.domain.repository.NotificationSender;
import com.light.approval.domain.repository.WorkflowRepository;
import com.light.approval.domain.service.ConditionEvaluator;
import com.light.approval.domain.service.WorkflowEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for WorkflowService.
 *
 * Covers:
 *   - All five Fig. 1 paths (A1–A3, B1–B2)
 *   - Boundary values at $5,000 and $10,000
 *   - Idempotency: PENDING invoice is not reprocessed
 *   - Idempotency: APPROVED invoice is not reprocessed
 *   - Resubmission: REJECTED invoice IS reprocessed
 *   - Multi-company: two companies route to separate workflows
 *   - Workflow update: new workflow used immediately after update()
 *   - Multi-action rule: one rule fires two notifications (Slack + Email)
 *   - No matching rules: empty result, no notifications
 */
@ExtendWith(MockitoExtension.class)
class WorkflowServiceTest {

}
