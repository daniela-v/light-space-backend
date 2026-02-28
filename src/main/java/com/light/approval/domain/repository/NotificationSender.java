package com.light.approval.domain.repository;

import com.light.approval.domain.model.ApprovalRequest;
import com.light.approval.domain.model.NotificationChannel;

public interface NotificationSender {
    NotificationChannel getChannel();
    void send(ApprovalRequest request);
}
