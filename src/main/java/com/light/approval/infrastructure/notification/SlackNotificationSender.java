package com.light.approval.infrastructure.notification;

import com.light.approval.domain.model.ApprovalRequest;
import com.light.approval.domain.model.NotificationChannel;
import com.light.approval.domain.repository.NotificationSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SlackNotificationSender implements NotificationSender {

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.SLACK;
    }

    @Override
    public void send(ApprovalRequest request) {
        log.info("[SLACK] Sending approval request via Slack @{} for invoice {} ({}{})",
            request.getApprover().getSlackHandle(),
            request.getInvoice().getId(),
            request.getInvoice().getMoney().getCurrency(),
            request.getInvoice().getMoney().getAmount());
    }
}
