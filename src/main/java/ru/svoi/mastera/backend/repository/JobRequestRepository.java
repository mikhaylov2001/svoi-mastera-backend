package ru.svoi.mastera.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.svoi.mastera.backend.entity.CustomerProfile;
import ru.svoi.mastera.backend.entity.JobRequest;

import java.util.List;
import java.util.UUID;

public interface JobRequestRepository extends JpaRepository<JobRequest, UUID>{
    List<JobRequest> findAllByCustomerOrderByCreatedAtDesc(CustomerProfile customer);
}
