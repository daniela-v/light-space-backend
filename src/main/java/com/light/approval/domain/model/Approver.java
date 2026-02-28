package com.light.approval.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "approver")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Approver {
    @Id
    private UUID id;
    private String name;
    @Enumerated(EnumType.STRING)
    private Department department;
    private String email;
    private String slackHandle;
    @Enumerated(EnumType.STRING)
    private NotificationChannel preferredChannel;
}
