package com.light.approval.domain.repository;

import com.light.approval.domain.model.Approver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface ApproverRepository extends JpaRepository<Approver, UUID> {}
