package ru.svoi.mastera.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.svoi.mastera.backend.entity.CustomerProfile;
import ru.svoi.mastera.backend.entity.Deal;
import ru.svoi.mastera.backend.entity.WorkerProfile;

import java.util.List;
import java.util.UUID;

public interface DealRepository extends JpaRepository<Deal, UUID> {

    List<Deal> findAllByCustomer(CustomerProfile customer);

    List<Deal> findAllByWorker(WorkerProfile worker);
}
