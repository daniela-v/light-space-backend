package com.light.approval.interfaces.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.light.approval.application.WorkflowService;
import com.light.approval.application.dto.InvoiceReceivedEvent;
import com.light.approval.application.dto.WorkflowCreateRequest;
import com.light.approval.domain.model.Company;
import com.light.approval.domain.model.Department;
import com.light.approval.domain.model.Invoice;
import com.light.approval.domain.model.Money;
import com.light.approval.domain.repository.CompanyRepository;
import com.light.approval.domain.repository.InvoiceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Scanner;
import java.util.UUID;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.cli.enabled", havingValue = "true", matchIfMissing = true)
public class InvoiceApprovalRunner implements ApplicationRunner {

    private final ApplicationEventPublisher eventPublisher;
    private final CompanyRepository companyRepository;
    private final InvoiceRepository invoiceRepository;
    private final WorkflowService workflowService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public InvoiceApprovalRunner(ApplicationEventPublisher eventPublisher,
                                 CompanyRepository companyRepository,
                                 InvoiceRepository invoiceRepository,
                                 WorkflowService workflowService) {
        this.eventPublisher = eventPublisher;
        this.companyRepository = companyRepository;
        this.invoiceRepository = invoiceRepository;
        this.workflowService = workflowService;
    }

    @Override
    public void run(ApplicationArguments args) {
        Scanner scanner = new Scanner(System.in);

        mainLoop:
        while (true) {
            System.out.println("\nSelect an option:");
            System.out.println("\n  [1] Submit an invoice");
            System.out.println("\n  [2] Create/update workflow (JSON input)");
            System.out.println("\n  [3] Exit");
            System.out.print("\nEnter choice: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    handleInvoiceJson(scanner);
                    break;
                case "2":
                    handleWorkflowJson(scanner);
                    break;
                case "3":
                    System.out.println("Exiting.");
                    break mainLoop;
                default:
                    System.out.println("Invalid choice. Please enter 1, 2, or 3.");
            }
        }

        scanner.close();
    }

    private void handleInvoiceJson(Scanner scanner) {
        try {
            System.out.print("\nEnter company ID (leave blank for default 00000000-0000-0000-0000-000000000001): ");
            String companyIdInput = scanner.nextLine().trim();
            UUID companyId = companyIdInput.isEmpty()
                    ? UUID.fromString("00000000-0000-0000-0000-000000000001")
                    : UUID.fromString(companyIdInput);

            Company company = companyRepository.findById(companyId)
                    .orElseThrow(() -> new IllegalArgumentException("Company not found: " + companyId));

            System.out.print("\nEnter invoice amount: ");
            BigDecimal amount = new BigDecimal(scanner.nextLine().trim());
            System.out.print("\nEnter department (Finance|Marketing): ");
            Department department = Department.valueOf(scanner.nextLine().trim().toUpperCase());
            System.out.print("\nRequires manager approval? (true|false): ");
            boolean requiresManagerApproval = Boolean.parseBoolean(scanner.nextLine().trim());

            Invoice invoice = new Invoice(UUID.randomUUID(), new Money(amount, "USD"), department, requiresManagerApproval, company);
            invoiceRepository.save(invoice);

            eventPublisher.publishEvent(new InvoiceReceivedEvent(this, invoice));
        } catch (Exception e) {
            log.error("Failed to submit invoice: {}", e.getMessage());
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void handleWorkflowJson(Scanner scanner) {
        try {
            System.out.println("\nPaste full workflow JSON and press Enter twice when done, e.g.:");
            String json = readMultilineInput(scanner);
            WorkflowCreateRequest request = objectMapper.readValue(json, WorkflowCreateRequest.class);
            workflowService.createWorkflow(request);
            System.out.println("Workflow created/updated successfully!");
        } catch (Exception e) {
            log.error("Failed to create/update workflow: {}", e.getMessage());
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Reads multiple lines from Scanner until a blank line is entered
     */
    private String readMultilineInput(Scanner scanner) {
        StringBuilder jsonBuilder = new StringBuilder();
        String line;
        while (!(line = scanner.nextLine()).isBlank()) {
            jsonBuilder.append(line);
        }
        return jsonBuilder.toString();
    }
}
