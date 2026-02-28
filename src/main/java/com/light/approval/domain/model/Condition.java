package com.light.approval.domain.model;

import java.util.UUID;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "conditions")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Condition {
    @Id
    private UUID id;
    private String field;
    @Enumerated(EnumType.STRING)
    private ConditionOperator operator;
    @Column(name = "condition_value")
    private String value;
    @ManyToOne
    @JoinColumn(name = "rule_id")
    private Rule rule;
    
    public static Condition of(String field, ConditionOperator operator, String value) {
        Condition condition = new Condition();
        condition.id = UUID.randomUUID();
        condition.field = field;
        condition.operator = operator;
        condition.value = value;
        return condition;
    }
}
