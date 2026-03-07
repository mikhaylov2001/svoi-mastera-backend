package ru.svoi.mastera.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.svoi.mastera.backend.entity.enams.DealStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "deals")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Deal extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_request_id", nullable = false)
    private JobRequest jobRequest;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_offer_id", nullable = false, unique = true)
    private JobOffer jobOffer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerProfile customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id", nullable = false)
    private WorkerProfile worker;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal agreedPrice;

    @Column(precision = 12, scale = 2)
    private BigDecimal platformFee;

    @Column(precision = 12, scale = 2)
    private BigDecimal payoutAmount;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private DealStatus status = DealStatus.NEW;

    @Column
    private Instant startedAt;

    @Column
    private Instant completedAt;

    @Column
    private Instant cancelledAt;

    @Column(length = 1000)
    private String cancellationReason;

    @OneToMany(mappedBy = "deal")
    private List<Payment> payments;

    @OneToMany(mappedBy = "deal")
    private List<Review> reviews;

}
