package ru.svoi.mastera.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.svoi.mastera.backend.entity.Review;
import ru.svoi.mastera.backend.entity.WorkerProfile;

import java.util.List;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    List<Review> findAllByTargetWorker(WorkerProfile targetWorker);
    boolean existsByDealId(UUID dealId);

    @Query("SELECT r FROM Review r WHERE r.authorUser.id = :userId ORDER BY r.createdAt DESC")
    List<Review> findAllByAuthorUserId(UUID userId);
}