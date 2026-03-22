package ru.svoi.mastera.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notification_settings")
@Data
public class NotificationSettings {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "new_deals", nullable = false)
    private Boolean newDeals = true;

    @Column(name = "messages", nullable = false)
    private Boolean messages = true;

    @Column(name = "reviews", nullable = false)
    private Boolean reviews = true;

    @Column(name = "system")
    private Boolean system = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}