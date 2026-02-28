package com.light.approval.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "invoice")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Invoice {
    @Id
    private UUID id;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "amount")),
            @AttributeOverride(name = "currency", column = @Column(name = "currency", length = 3))
    })
    private Money money;

    @Enumerated(EnumType.STRING)
    private Department department;

    private Boolean requiresManagerApproval;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;
}
