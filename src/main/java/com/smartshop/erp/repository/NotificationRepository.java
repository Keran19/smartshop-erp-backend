package com.smartshop.erp.repository;

import com.smartshop.erp.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUtilisateur_IdUtilisateurAndVueOrderByDateCreationDesc(Long idUtilisateur, Boolean vue);
}
