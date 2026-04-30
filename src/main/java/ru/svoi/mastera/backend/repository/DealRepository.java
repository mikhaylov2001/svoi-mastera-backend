package ru.svoi.mastera.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.svoi.mastera.backend.entity.CustomerProfile;
import ru.svoi.mastera.backend.entity.Deal;
import ru.svoi.mastera.backend.entity.WorkerProfile;
import ru.svoi.mastera.backend.entity.enams.DealStatus;

import java.util.List;
import java.util.UUID;

public interface DealRepository extends JpaRepository<Deal, UUID> {

    List<Deal> findAllByCustomer(CustomerProfile customer);

    List<Deal> findAllByWorker(WorkerProfile worker);

    long countByListingIdAndStatus(UUID listingId, DealStatus status);

    boolean existsByListingIdAndStatus(UUID listingId, DealStatus status);

    /**
     * Есть ли у заказчика по этому объявлению сделка не в финальных статусах отмены/возврата
     * (активная или завершённая — повторно «принять объявление» нельзя).
     */
    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END FROM Deal d WHERE d.listingId = :listingId AND d.customer.user.id = :userId AND d.status NOT IN ('CANCELLED', 'REFUNDED')")
    boolean existsNonCancelledDealForListingAndCustomerUser(@Param("listingId") UUID listingId, @Param("userId") UUID userId);
}
