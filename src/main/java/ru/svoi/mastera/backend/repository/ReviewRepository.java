package ru.svoi.mastera.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.svoi.mastera.backend.entity.CustomerProfile;
import ru.svoi.mastera.backend.entity.Review;
import ru.svoi.mastera.backend.entity.WorkerProfile;

import java.util.List;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    List<Review> findAllByTargetWorker(WorkerProfile targetWorker);
    boolean existsByDealId(UUID dealId);

    // Существует ли отзыв заказчика → мастеру по сделке
    // (targetWorker IS NOT NULL означает что это отзыв мастеру)
    @Query("SELECT COUNT(r) > 0 FROM Review r WHERE r.deal.id = :dealId AND r.targetWorker IS NOT NULL")
    boolean existsCustomerReviewByDealId(UUID dealId);

    // Существует ли отзыв мастера → заказчику по сделке
    @Query("SELECT COUNT(r) > 0 FROM Review r WHERE r.deal.id = :dealId AND r.targetCustomer IS NOT NULL")
    boolean existsWorkerReviewByDealId(UUID dealId);

    List<Review> findAllByTargetCustomerOrderByCreatedAtDesc(CustomerProfile targetCustomer);

    List<Review> findAllByTargetCustomer(CustomerProfile targetCustomer);


}