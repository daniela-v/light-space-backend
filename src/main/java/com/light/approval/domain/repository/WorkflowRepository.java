package com.light.approval.domain.repository;

import com.light.approval.domain.model.Workflow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkflowRepository extends JpaRepository<Workflow, UUID> {

    @Query("""
        SELECT w FROM Workflow w
        JOIN FETCH w.rules r
        WHERE w.company.id = :companyId AND w.active = true
        """)
    Optional<Workflow> findActiveWorkflowWithRules(UUID companyId);


    @Modifying
    @Query("""
        UPDATE Workflow w
        SET w.active = false
        WHERE w.company.id = :companyId
        AND w.active = true
        """)
    int deactivateActiveWorkflow(UUID companyId);
}
