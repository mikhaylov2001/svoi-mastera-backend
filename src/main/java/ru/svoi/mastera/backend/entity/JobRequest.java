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
import ru.svoi.mastera.backend.entity.enams.JobRequestStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "job_requests")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JobRequest extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerProfile customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 4000)
    private String description;

    @Column(length = 500)
    private String addressText;

    @Column(length = 255)
    private String city;

    @Column
    private Instant scheduledAt;

    @Column(precision = 12, scale = 2)
    private BigDecimal budgetFrom;

    @Column(precision = 12, scale = 2)
    private BigDecimal budgetTo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private JobRequestStatus status = JobRequestStatus.DRAFT;

    @OneToMany(mappedBy = "jobRequest")
    private List<JobOffer> offers;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_offer_id")
    private JobOffer selectedOffer;

}
