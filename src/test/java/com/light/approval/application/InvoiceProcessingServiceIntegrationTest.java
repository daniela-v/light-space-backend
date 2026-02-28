package com.light.approval.application;

import com.light.approval.domain.model.*;
import com.light.approval.domain.repository.ApprovalRequestRepository;
import com.light.approval.domain.repository.InvoiceRepository;
import com.light.approval.domain.repository.NotificationSender;
import com.light.approval.domain.repository.WorkflowRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = "app.cli.enabled=false")
@Transactional
class InvoiceProcessingServiceIntegrationTest {

    @Autowired
    private InvoiceProcessingService invoiceProcessingService;

    @Autowired
    private ApprovalRequestRepository approvalRequestRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private WorkflowRepository workflowRepository;

    private NotificationSender spyNotificationSender;

    private final UUID companyId = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @BeforeEach
    void setupNotificationSpy() {
        // Create a spy NotificationSender that matches the approver's channel
        spyNotificationSender = spy(NotificationSender.class);
        when(spyNotificationSender.getChannel()).thenReturn(NotificationChannel.SLACK);

        // Replace the service's sender list
        invoiceProcessingService.notificationSenders.clear();
        invoiceProcessingService.notificationSenders.add(spyNotificationSender);
    }

    @Test
    void execute_shouldCreateApprovalRequestAndTriggerNotificationOnce() throws InterruptedException {
        Workflow workflow = workflowRepository.findActiveWorkflowWithRules(companyId).orElseThrow();

        Invoice invoice = new Invoice(
                UUID.randomUUID(),
                new Money(new BigDecimal("3000"), "USD"),
                Department.FINANCE,
                false,
                workflow.getCompany()
        );
        invoiceRepository.save(invoice);

        Action action = workflow.getRules().get(0).getActions().get(0);

        // Use CountDownLatch to wait for async notification
        CountDownLatch latch = new CountDownLatch(1);
        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(spyNotificationSender).send(any());

        invoiceProcessingService.execute(invoice, action);

        // Wait for the async task
        latch.await(2, TimeUnit.SECONDS);

        // Verify ApprovalRequest is created
        assertThat(approvalRequestRepository.existsByInvoiceIdAndStatusIn(
                invoice.getId(),
                List.of(ApprovalStatus.PENDING)
        )).isTrue();

        // Verify notification triggered exactly once
        verify(spyNotificationSender, times(1)).send(any());
    }
}
