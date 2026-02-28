package com.light.approval.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "action")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Action {
    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    private ActionType actionType;

    @ManyToOne
    @JoinColumn(name = "rule_id")
    private Rule rule;

    @ManyToOne
    @JoinColumn(name = "approver_id")
    private Approver approver;

    public static Action of(Rule rule, ActionType actionType, Approver approver) {
        Action action = new Action();
        action.id = UUID.randomUUID();
        action.rule = rule;
        action.actionType = actionType;
        action.approver = approver;
        return action;
    }
}
