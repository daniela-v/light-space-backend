package com.light.approval.application.dto;

import com.light.approval.domain.model.Invoice;
import org.springframework.context.ApplicationEvent;

public class InvoiceReceivedEvent extends ApplicationEvent {

    private final Invoice invoice;

    public InvoiceReceivedEvent(Object source, Invoice invoice) {
        super(source);
        this.invoice = invoice;
    }

    public Invoice getInvoice() {
        return invoice;
    }
}
