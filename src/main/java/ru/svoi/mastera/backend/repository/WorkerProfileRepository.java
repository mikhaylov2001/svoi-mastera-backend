package ru.svoi.mastera.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.svoi.mastera.backend.entity.WorkerProfile;
import ru.svoi.mastera.backend.entity.User;

import java.util.Optional;
import java.util.UUID;

public interface WorkerProfileRepository extends JpaRepository<WorkerProfile, UUID> {

    Optional<WorkerProfile> findByUser(User user);

    @Query("SELECT w FROM WorkerProfile w JOIN FETCH w.user WHERE w.user.id = :userId")
    Optional<WorkerProfile> findByUserId(@Param("userId") UUID userId);
}
