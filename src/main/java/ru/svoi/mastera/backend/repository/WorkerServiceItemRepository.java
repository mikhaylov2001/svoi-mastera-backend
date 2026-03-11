package ru.svoi.mastera.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.svoi.mastera.backend.entity.WorkerProfile;
import ru.svoi.mastera.backend.entity.WorkerServiceItem;

import java.util.List;
import java.util.UUID;

public interface WorkerServiceItemRepository extends JpaRepository<WorkerServiceItem, UUID> {
    List<WorkerServiceItem> findAllByWorkerProfileOrderByCreatedAtDesc(WorkerProfile workerProfile);
    List<WorkerServiceItem> findAllByWorkerProfileAndActiveTrueOrderByCreatedAtDesc(WorkerProfile workerProfile);
}

