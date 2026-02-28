package com.light.approval.application;

import com.light.approval.domain.model.Department;
import com.light.approval.domain.model.Invoice;

import java.math.BigDecimal;
import java.util.Arrays;

public class InvoiceValidator {

    public static void validateInvoice(Invoice invoice) {
        if (invoice == null) {
            throw new RuntimeException("Invoice is null");
        }

        if (invoice.getId() == null) {
            throw new RuntimeException("Invoice must have a UUID id");
        }

        if (invoice.getMoney().getAmount() == null || invoice.getMoney().getAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Invoice amount must be non-null and >= 0");
        }

        if (invoice.getDepartment() == null) {
            throw new RuntimeException("Invoice department must be set");
        }

        boolean validDepartment = Arrays.stream(Department.values())
                .anyMatch(d -> d == invoice.getDepartment());
        if (!validDepartment) {
            throw new RuntimeException(
                    "Invalid department: " + invoice.getDepartment() +
                            ". Must be one of: " +
                            String.join(", ", Arrays.stream(Department.values()).map(Enum::name).toList())
            );
        }

        if (invoice.getRequiresManagerApproval() == null) {
            throw new RuntimeException("requiresManagerApproval must be set to true or false");
        }

        if (invoice.getCompany() == null || invoice.getCompany().getId() == null) {
            throw new RuntimeException("Invoice must have a valid company");
        }
    }
}
