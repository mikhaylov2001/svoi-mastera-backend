package ru.svoi.mastera.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.svoi.mastera.backend.entity.JobOffer;
import ru.svoi.mastera.backend.entity.JobRequest;
import ru.svoi.mastera.backend.entity.WorkerProfile;

import java.util.List;
import java.util.UUID;

public interface JobOfferRepository extends JpaRepository<JobOffer, UUID> {

    List<JobOffer> findAllByJobRequest(JobRequest jobRequest);

    List<JobOffer> findAllByWorker(WorkerProfile worker);

    long countByJobRequest_Id(UUID jobRequestId);

    /** Есть ли у мастера по этой заявке отклик, который ещё нельзя дублировать (после отмены сделки старый — REJECTED и не считается). */
    @Query("SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END FROM JobOffer o WHERE o.jobRequest.id = :jrId AND o.worker.id = :wid AND o.status NOT IN ('REJECTED', 'WITHDRAWN', 'EXPIRED')")
    boolean existsOpenLikeOfferFromWorker(@Param("jrId") UUID jobRequestId, @Param("wid") UUID workerId);
}
