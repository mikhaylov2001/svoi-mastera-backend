package ru.svoi.mastera.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.svoi.mastera.backend.entity.WorkerProfile;
import ru.svoi.mastera.backend.entity.User;

import java.util.Optional;
import java.util.UUID;

public interface WorkerProfileRepository extends JpaRepository<WorkerProfile, UUID> {

    Optional<WorkerProfile> findByUser(User user);
    Optional<WorkerProfile> findByUserId(UUID userId);

}
