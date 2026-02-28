package com.light.approval.application.dto;

import lombok.Data;
import java.util.List;
import java.util.UUID;


import com.light.approval.domain.model.ActionType;
import com.light.approval.domain.model.ConditionOperator;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
public class WorkflowCreateRequest {
    private UUID companyId;
    private String name;
    private List<RuleRequest> rules;

    @Data
    public static class RuleRequest {
        private String name;
        private int stepOrder;
        private List<ConditionRequest> conditions;
        private List<ActionRequest> actions;
    }

    @Data
    public static class ConditionRequest {
        private String field;
        private ConditionOperator operator;
        private String value;
    }

    @Data
    public static class ActionRequest {
        private ActionType actionType;
        private UUID approverId;
    }
}
