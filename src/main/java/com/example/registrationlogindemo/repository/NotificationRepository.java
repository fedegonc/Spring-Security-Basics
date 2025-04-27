package com.example.registrationlogindemo.repository;

import com.example.registrationlogindemo.entity.Notification;
import com.example.registrationlogindemo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    List<Notification> findByUserOrderByCreatedAtDesc(User user);
    
    List<Notification> findByUserAndReadOrderByCreatedAtDesc(User user, boolean read);
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user = ?1 AND n.read = false")
    long countUnreadNotifications(User user);
    
    List<Notification> findByRelatedEntityTypeAndRelatedEntityId(String entityType, Long entityId);
}
