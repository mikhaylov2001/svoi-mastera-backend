package ru.svoi.mastera.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.svoi.mastera.backend.entity.enams.UserStatus;

import java.time.Instant;

@Entity
@Table(name = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User extends BaseEntity{

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(unique = true, length = 30)
    private String phone;

    @Column(nullable = false, length = 255)
    private String passwordHash;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private UserStatus status = UserStatus.PENDING_VERIFICATION;

    @Column
    private Instant lastLoginAt;

    @OneToOne(mappedBy = "user")
    private WorkerProfile workerProfile;

    @OneToOne(mappedBy = "user")
    private CustomerProfile customerProfile;

}
