package com.light.approval.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "workflow")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Workflow {
    @Id
    private UUID id;
    private String name;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;

    @OneToMany(mappedBy = "workflow", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("stepOrder ASC")
    private List<Rule> rules;

    private boolean active;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public static Workflow createNew(Company company, String name, List<Rule> rules) {
        Workflow workflow = new Workflow();
        workflow.id = UUID.randomUUID();
        workflow.company = company;
        workflow.name = name;
        workflow.rules = rules;
        workflow.active = true;
        rules.forEach(r -> r.setWorkflow(workflow));
        return workflow;
    }

    public void deactivate() {
        this.active = false;
    }
}
