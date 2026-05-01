package ru.svoi.mastera.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.svoi.mastera.backend.entity.enams.VerificationStatus;

import java.time.Instant;
import java.util.List;
@Entity
@Table(name = "customer_profiles")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerProfile extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, length = 150)
    private String displayName;

    @Column(length = 255)
    private String city;

    @OneToMany(mappedBy = "customer")
    private List<JobRequest> jobRequests;

    private String phone;

    @Column(length = 150)
    private String lastName;

    @Column(nullable = false)
    private boolean verified = false;

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
}

