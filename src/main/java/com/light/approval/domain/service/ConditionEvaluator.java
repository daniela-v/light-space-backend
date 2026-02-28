package com.light.approval.domain.service;

import com.light.approval.domain.model.Condition;
import com.light.approval.domain.model.ConditionOperator;
import com.light.approval.domain.model.Invoice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Evaluates a list of conditions against an invoice
 * Supported fields:
 *   "amount"                   — resolved as BigDecimal, numeric operators apply
 *   "department"               — resolved as String,     EQ/NEQ apply
 *   "requiresManagerApproval"  — resolved as boolean,    EQ/NEQ apply ("true"/"false")
 */
@Slf4j
@Component
public class ConditionEvaluator {

    public boolean allMatch(List<Condition> conditions, Invoice invoice) {
        return conditions.stream().allMatch(c -> evaluate(c, invoice));
    }

    private boolean evaluate(Condition condition, Invoice invoice) {
        String field = condition.getField();
        ConditionOperator op = condition.getOperator();
        String rawValue = condition.getValue();

        try {
            return switch (field) {
                case "amount" -> compareNumeric(invoice.getMoney().getAmount(), op, new BigDecimal(rawValue));
                case "department" -> compareString(invoice.getDepartment().toString(), op, rawValue.toLowerCase());
                case "requiresManagerApproval" ->
                        compareString(String.valueOf(invoice.getRequiresManagerApproval()).toLowerCase(), op, rawValue.toLowerCase());
                default -> {
                    log.warn("Unknown condition field '{}' — condition skipped (treated as non-matching)", field);
                    yield false;
                }
            };
        } catch (NumberFormatException e) {
            log.warn("Cannot parse '{}' as number for field '{}' — condition non-matching", rawValue, field);
            return false;
        }
    }

    private boolean compareNumeric(BigDecimal actual, ConditionOperator op, BigDecimal expected) {
        int cmp = actual.compareTo(expected);
        return switch (op) {
            case EQ  -> cmp == 0;
            case NEQ -> cmp != 0;
            case GT  -> cmp >  0;
            case GTE -> cmp >= 0;
            case LT  -> cmp <  0;
            case LTE -> cmp <= 0;
        };
    }

    private boolean compareString(String actual, ConditionOperator op, String expected) {
        boolean eq = actual.equalsIgnoreCase(expected);
        return switch (op) {
            case EQ  ->  eq;
            case NEQ -> !eq;
            default  -> {
                log.warn("Operator {} is not valid for string field — condition non-matching", op);
                yield false;
            }
        };
    }
}
