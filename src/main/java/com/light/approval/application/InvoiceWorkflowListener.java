package com.light.approval.application;

import com.light.approval.domain.model.Invoice;
import com.light.approval.application.dto.InvoiceReceivedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InvoiceWorkflowListener {

    private final WorkflowService workflowService;

    public InvoiceWorkflowListener(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @Async
    @EventListener
    public void handleInvoiceEvent(InvoiceReceivedEvent event) {
        Invoice invoice = event.getInvoice();
        try {
            InvoiceValidator.validateInvoice(invoice);
            workflowService.process(invoice);
        } catch (RuntimeException e) {
            log.error("Invoice {} failed validation: {}", invoice.getId(), e.getMessage());
            throw e;
        }
    }
}
