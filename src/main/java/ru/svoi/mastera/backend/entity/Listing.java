package ru.svoi.mastera.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "listings")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Listing extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id", nullable = false)
    private WorkerProfile worker;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer price;

    @Column(length = 100)
    private String priceUnit; // за час, за работу, договорная

    @Column(length = 100)
    private String category;

    @Column(columnDefinition = "TEXT[]")
    private String[] photos;

    @Column(nullable = false)
    private boolean active = true;

    /** Счётчик просмотров публичной страницы объявления */
    @Column(name = "view_count", nullable = false)
    private int viewCount = 0;
}