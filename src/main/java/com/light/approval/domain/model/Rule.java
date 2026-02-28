package com.light.approval.domain.model;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "rule", uniqueConstraints = @UniqueConstraint(columnNames = {"workflow_id", "stepOrder"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Rule {
    @Id
    private UUID id;
    private String name;
    private int stepOrder;

    @ManyToOne
    @JoinColumn(name = "workflow_id")
    private Workflow workflow;

    @OneToMany(mappedBy = "rule", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Condition> conditions;

    @OneToMany(mappedBy = "rule", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Action> actions;

    public static Rule of(String name, int stepOrder, List<Condition> conditions, List<Action> actions) {
        Rule rule = new Rule();
        rule.id = UUID.randomUUID();
        rule.name = name;
        rule.stepOrder = stepOrder;
        rule.conditions = conditions;
        rule.actions = actions;
        conditions.forEach(c -> c.setRule(rule));
        actions.forEach(a -> a.setRule(rule));
        return rule;
    }
}
