package com.light.approval.domain.model;

/**
 * Comparison operators available for rule conditions.
 *
 * These are stored as strings in the DB condition.operator column.
 * Adding a new operator requires only extending this enum and the
 * ConditionEvaluator switch — no schema change needed.
 *
 */
public enum ConditionOperator {
    EQ,   // field == value  (string equality, case-insensitive)
    NEQ,  // field != value
    GT,   // field >  value  (numeric)
    GTE,  // field >= value  (numeric)
    LT,   // field <  value  (numeric)
    LTE   // field <= value  (numeric)
}
