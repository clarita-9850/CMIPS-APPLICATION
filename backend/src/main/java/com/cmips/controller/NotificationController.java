package com.cmips.controller;

import com.cmips.annotation.RequirePermission;
import com.cmips.entity.Notification;
import com.cmips.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }
    
    @GetMapping("/user/{userId}")
    @RequirePermission(resource = "Normal Login Resource", scope = "view")
    public ResponseEntity<List<Notification>> getUserNotifications(@PathVariable String userId) {
        List<Notification> notifications = notificationService.getUserNotifications(userId);
        return ResponseEntity.ok(notifications);
    }
    
    @GetMapping("/unread-count/{userId}")
    @RequirePermission(resource = "Normal Login Resource", scope = "view")
    public ResponseEntity<Long> getUnreadCount(@PathVariable String userId) {
        Long count = notificationService.getUnreadNotificationCount(userId);
        return ResponseEntity.ok(count);
    }
    
    @PutMapping("/{id}/read")
    @RequirePermission(resource = "Normal Login Resource", scope = "view")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.noContent().build();
    }
    
    @PutMapping("/user/{userId}/read-all")
    @RequirePermission(resource = "Normal Login Resource", scope = "view")
    public ResponseEntity<Void> markAllAsRead(@PathVariable String userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.noContent().build();
    }
    
    @DeleteMapping("/{id}")
    @RequirePermission(resource = "Normal Login Resource", scope = "delete")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping
    @RequirePermission(resource = "Normal Login Resource", scope = "create")
    public ResponseEntity<Notification> createNotification(@RequestBody Notification notification) {
        Notification created = notificationService.createNotification(notification);
        return ResponseEntity.ok(created);
    }
}




