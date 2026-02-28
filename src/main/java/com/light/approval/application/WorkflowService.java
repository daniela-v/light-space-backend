package com.light.approval.application;

import com.light.approval.application.dto.WorkflowCreateRequest;
import com.light.approval.domain.model.*;
import com.light.approval.domain.repository.ApproverRepository;
import com.light.approval.domain.repository.CompanyRepository;
import com.light.approval.domain.repository.InvoiceRepository;
import com.light.approval.domain.repository.WorkflowRepository;
import com.light.approval.domain.service.WorkflowEngine;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class WorkflowService {

    private final WorkflowRepository workflowRepository;
    private final WorkflowEngine workflowEngine;
    private final InvoiceProcessingService invoiceProcessingService;
    private final CompanyRepository companyRepository;
    private final ApproverRepository approverRepository;

    public WorkflowService(WorkflowRepository workflowRepository,
                           WorkflowEngine workflowEngine,
                           InvoiceProcessingService invoiceProcessingService, InvoiceRepository invoiceRepository, CompanyRepository companyRepository, ApproverRepository approverRepository) {
        this.workflowRepository = workflowRepository;
        this.workflowEngine = workflowEngine;
        this.invoiceProcessingService = invoiceProcessingService;
        this.companyRepository = companyRepository;
        this.approverRepository = approverRepository;
    }

    @Transactional
    public void process(Invoice invoice) {
        Workflow workflow = workflowRepository.findActiveWorkflowWithRules(invoice.getCompany().getId())
                .orElseThrow(() -> new IllegalStateException("No workflow configured for company: " + invoice.getCompany().getId()));
        List<Action> actions = workflowEngine.evaluate(invoice, workflow);
        for (Action action : actions) {
            invoiceProcessingService.execute(invoice, action);
        }
    }

    @Transactional
    public Workflow createWorkflow(WorkflowCreateRequest request) {
        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new IllegalArgumentException("Company not found: " + request.getCompanyId()));

        workflowRepository.deactivateActiveWorkflow(request.getCompanyId());

        List<Rule> rules = request.getRules().stream()
                .map(this::buildRule)
                .toList();

        return workflowRepository.save(Workflow.createNew(company, request.getName(), rules));
    }

    private Rule buildRule(WorkflowCreateRequest.RuleRequest r) {
        List<Condition> conditions = r.getConditions().stream()
                .map(c -> Condition.of(c.getField(), c.getOperator(), c.getValue()))
                .toList();

        List<Action> actions = r.getActions().stream()
                .map(a -> {
                    Approver approver = approverRepository.findById(a.getApproverId())
                            .orElseThrow(() -> new IllegalArgumentException("Approver not found: " + a.getApproverId()));
                    return Action.of(null, a.getActionType(), approver); // Rule wired later
                }).toList();

        return Rule.of(r.getName(), r.getStepOrder(), conditions, actions);
    }
}
