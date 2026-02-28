package com.light.approval.domain.model;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

@Embeddable
@NoArgsConstructor
@Getter
public class Money {

    private BigDecimal amount;
    private String currency;

    public Money(BigDecimal amount, String currency) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount must be non-negative");
        }
        Currency cur = Currency.getInstance(currency);
        this.currency = cur.getCurrencyCode();
        this.amount = amount.setScale(cur.getDefaultFractionDigits(), RoundingMode.HALF_UP);
    }
}
