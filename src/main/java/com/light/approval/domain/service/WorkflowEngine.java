package com.light.approval.domain.service;

import com.light.approval.domain.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
public class WorkflowEngine {

    private final ConditionEvaluator conditionEvaluator;

    public WorkflowEngine(ConditionEvaluator conditionEvaluator) {
        this.conditionEvaluator = conditionEvaluator;
    }

    public List<Action> evaluate(Invoice invoice, Workflow workflow) {
        return workflow.getRules().stream()
            .sorted(Comparator.comparingInt(Rule::getStepOrder))
            .filter(rule -> conditionEvaluator.allMatch(rule.getConditions(), invoice))
            .flatMap(rule -> rule.getActions().stream())
            .toList();
    }
}

