package com.smartshop.erp.repository;

import com.smartshop.erp.entity.DetailCoupureSession;
import com.smartshop.erp.enums.TypeOperationCoupure;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DetailCoupureSessionRepository extends JpaRepository<DetailCoupureSession, Long> {
    Optional<DetailCoupureSession> findBySession_IdSessionAndTypeOperation(Long idSession, TypeOperationCoupure typeOperation);
}
