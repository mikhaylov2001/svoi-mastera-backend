package ru.svoi.mastera.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.svoi.mastera.backend.entity.Listing;
import ru.svoi.mastera.backend.entity.WorkerProfile;

import java.util.List;
import java.util.UUID;

public interface ListingRepository extends JpaRepository<Listing, UUID> {

    @Query("SELECT l FROM Listing l LEFT JOIN FETCH l.worker w LEFT JOIN FETCH w.user WHERE l.worker = :worker ORDER BY l.createdAt DESC")
    List<Listing> findAllByWorkerOrderByCreatedAtDesc(WorkerProfile worker);

    @Query("SELECT l FROM Listing l LEFT JOIN FETCH l.worker w LEFT JOIN FETCH w.user WHERE l.active = true ORDER BY l.createdAt DESC")
    List<Listing> findAllActiveOrderByCreatedAtDesc();

    @Query("SELECT l FROM Listing l LEFT JOIN FETCH l.worker w LEFT JOIN FETCH w.user WHERE l.active = true AND l.category = :category ORDER BY l.createdAt DESC")
    List<Listing> findAllActiveByCategoryOrderByCreatedAtDesc(String category);
}