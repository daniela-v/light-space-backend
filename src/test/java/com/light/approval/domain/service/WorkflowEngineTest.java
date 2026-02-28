package com.light.approval.domain.service;

import com.light.approval.domain.model.*;
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
class WorkflowEngineTest {

    @Autowired
    private WorkflowEngine workflowEngine;

    @Autowired
    private WorkflowRepository workflowRepository;

    private final UUID companyId = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Test
    void shouldEvaluateCorrectActionsForAllInvoiceTypes() {
        Workflow workflow = workflowRepository.findActiveWorkflowWithRules(companyId).orElseThrow();

        // Small invoice → A1 → Finance Team Member
        Invoice smallInvoice = new Invoice(
                UUID.randomUUID(),
                new Money(new BigDecimal("3000"), "USD"),
                Department.FINANCE,
                false,
                workflow.getCompany()
        );

        List<Action> actions = workflowEngine.evaluate(smallInvoice, workflow);
        assertThat(actions).hasSize(1);
        assertThat(actions.get(0).getApprover().getName()).isEqualTo("Finance Team Member");

        // Mid invoice, no manager → A2
        Invoice midNoManager = new Invoice(
                UUID.randomUUID(),
                new Money(new BigDecimal("7000"), "USD"),
                Department.FINANCE,
                false,
                workflow.getCompany()
        );
        actions = workflowEngine.evaluate(midNoManager, workflow);
        assertThat(actions).hasSize(1);
        assertThat(actions.get(0).getApprover().getName()).isEqualTo("Finance Team Member");

        // Mid invoice, requires manager → A3
        Invoice midWithManager = new Invoice(
                UUID.randomUUID(),
                new Money(new BigDecimal("7000"), "USD"),
                Department.FINANCE,
                true,
                workflow.getCompany()
        );
        actions = workflowEngine.evaluate(midWithManager, workflow);
        assertThat(actions).hasSize(1);
        assertThat(actions.get(0).getApprover().getName()).isEqualTo("Finance Manager");

        // Large non-marketing → B1
        Invoice largeNonMarketing = new Invoice(
                UUID.randomUUID(),
                new Money(new BigDecimal("15000"), "USD"),
                Department.FINANCE,
                true,
                workflow.getCompany()
        );
        actions = workflowEngine.evaluate(largeNonMarketing, workflow);
        assertThat(actions).hasSize(1);
        assertThat(actions.get(0).getApprover().getName()).isEqualTo("CFO");

        // Large marketing → B2
        Invoice largeMarketing = new Invoice(
                UUID.randomUUID(),
                new Money(new BigDecimal("15000"), "USD"),
                Department.MARKETING,
                true,
                workflow.getCompany()
        );
        actions = workflowEngine.evaluate(largeMarketing, workflow);
        assertThat(actions).hasSize(1);
        assertThat(actions.get(0).getApprover().getName()).isEqualTo("CMO");
    }
}
