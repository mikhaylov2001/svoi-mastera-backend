package ru.svoi.mastera.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "worker_services")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkerServiceItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_profile_id", nullable = false)
    private WorkerProfile workerProfile;

    // ✅ ДОБАВЛЕНО: Связь с категорией
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(precision = 12, scale = 2)
    private BigDecimal priceFrom;

    @Column(precision = 12, scale = 2)
    private BigDecimal priceTo;

    @Column(nullable = false)
    private boolean active = true;
}