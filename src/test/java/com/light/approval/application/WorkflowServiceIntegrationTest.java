package com.light.approval.application;

import com.light.approval.domain.model.*;
import com.light.approval.domain.repository.ApprovalRequestRepository;
import com.light.approval.domain.repository.InvoiceRepository;
import com.light.approval.domain.repository.WorkflowRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "app.cli.enabled=false")
@Transactional
class WorkflowServiceIntegrationTest {

    @Autowired
    private WorkflowService workflowService;

    @Autowired
    private WorkflowRepository workflowRepository;

    @Autowired
    private ApprovalRequestRepository approvalRequestRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    private final UUID companyId = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Test
    void process_shouldCreateApprovalRequestsForInvoice() {
        Workflow workflow = workflowRepository.findActiveWorkflowWithRules(companyId).orElseThrow();

        Invoice invoice = new Invoice(
                UUID.randomUUID(),
                new Money(new BigDecimal("3000"), "USD"),
                Department.FINANCE,
                false,
                workflow.getCompany()
        );

        // Persist the invoice before processing
        invoiceRepository.save(invoice);

        workflowService.process(invoice);

        assertThat(approvalRequestRepository.existsByInvoiceIdAndStatusIn(
                invoice.getId(),
                List.of(ApprovalStatus.PENDING)
        )).isTrue();
    }
}
