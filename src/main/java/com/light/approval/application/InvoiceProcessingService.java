package com.light.approval.application;

import com.light.approval.domain.model.*;
import com.light.approval.domain.repository.ApprovalRequestRepository;
import com.light.approval.domain.repository.NotificationSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.light.approval.domain.model.ApprovalStatus.APPROVED;
import static com.light.approval.domain.model.ApprovalStatus.PENDING;

@Service
@Slf4j
public class InvoiceProcessingService {

    private final ApprovalRequestRepository approvalRequestRepository;

    private final List<NotificationSender> notificationSenders;
    private final ExecutorService notificationExecutor = Executors.newFixedThreadPool(2);

    public InvoiceProcessingService(ApprovalRequestRepository approvalRequestRepository, List<NotificationSender> notificationSenders) {
        this.approvalRequestRepository = approvalRequestRepository;
        this.notificationSenders = notificationSenders;
    }

    public void execute(Invoice invoice, Action action) {
        switch (action.getActionType()) {
            case CREATE_APPROVAL_REQUEST -> handleApprovalRequest(invoice, action);
            default -> log.warn("Unhandled action type {}", action.getActionType());
        }
    }

    private void handleApprovalRequest(Invoice invoice, Action action) {
        if (approvalRequestRepository.existsByInvoiceIdAndStatusIn(invoice.getId(), List.of(PENDING, APPROVED))) {
            log.info("Invoice {} already has active approval request — skipping", invoice.getId());
            return;
        }

        ApprovalRequest request = new ApprovalRequest(invoice, action.getRule(), action.getApprover());
        approvalRequestRepository.save(request);

        if (action.getApprover() != null) {
            notificationExecutor.submit(() -> sendNotification(request, action.getApprover().getPreferredChannel()));
        }
    }

    private void sendNotification(ApprovalRequest request, NotificationChannel channel) {
        notificationSenders.stream()
                .filter(sender -> sender.getChannel() == channel)
                .findFirst()
                .ifPresentOrElse(sender -> {
                    notificationExecutor.submit(() -> {
                        try {
                            sender.send(request);
                        } catch (Exception e) {
                            log.error("Failed to send notification for request {} via {}", request.getId(), channel, e);
                        }
                    });
                }, () -> log.warn("No sender found for channel {}", channel));
    }
}
