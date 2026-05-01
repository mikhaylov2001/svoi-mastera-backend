package ru.svoi.mastera.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.svoi.mastera.backend.entity.enams.VerificationStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "worker_profiles")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkerProfile extends BaseEntity{
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    private String displayName;
    private String about;
    private String city;
    private Integer experienceYears;

    @Column(nullable = false, precision = 3, scale = 2)
    private BigDecimal ratingAvg = BigDecimal.ZERO;

    @Column(nullable = false)
    private Integer reviewsCount = 0;

    @Column(nullable = false)
    private boolean verified = false;

    @Column(nullable = false)
    private boolean active = true;

    @ManyToMany
    @JoinTable(
            name = "worker_profile_categories",
            joinColumns = @JoinColumn(name = "worker_profile_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories = new HashSet<>();

    @OneToMany(mappedBy = "worker")
    private List<JobOffer> jobOffers;

    @OneToMany(mappedBy = "targetWorker")
    private List<Review> receivedReviews;

    private String phone;

    @Column(length = 150)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 32)
    private VerificationStatus verificationStatus = VerificationStatus.NONE;

    @Column(name = "verification_submitted_at")
    private Instant verificationSubmittedAt;

    @Column(name = "verification_documents_json", columnDefinition = "TEXT")
    private String verificationDocumentsJson;

    @Column(name = "verification_signature_json", columnDefinition = "TEXT")
    private String verificationSignatureJson;

    @Column(name = "verification_rejection_reason", length = 500)
    private String verificationRejectionReason;

    @Column(name = "guarantee_terms_accepted_at")
    private Instant guaranteeTermsAcceptedAt;

    @Column(name = "guarantee_terms_consent_json", columnDefinition = "TEXT")
    private String guaranteeTermsConsentJson;
}
