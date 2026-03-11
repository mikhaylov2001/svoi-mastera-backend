package ru.svoi.mastera.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.svoi.mastera.backend.dto.ReviewCreateDto;
import ru.svoi.mastera.backend.dto.ReviewDto;
import ru.svoi.mastera.backend.entity.Deal;
import ru.svoi.mastera.backend.entity.Review;
import ru.svoi.mastera.backend.entity.User;
import ru.svoi.mastera.backend.entity.WorkerProfile;
import ru.svoi.mastera.backend.entity.CustomerProfile;
import ru.svoi.mastera.backend.entity.enams.DealStatus;
import ru.svoi.mastera.backend.repository.DealRepository;
import ru.svoi.mastera.backend.repository.ReviewRepository;
import ru.svoi.mastera.backend.repository.WorkerProfileRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private DealRepository dealRepository;
    @Mock
    private WorkerProfileRepository workerProfileRepository;

    @InjectMocks
    private ReviewService reviewService;

    @Test
    void createReviewRequiresCompletedDealAndOwner() {
        UUID customerId = UUID.randomUUID();
        UUID dealId = UUID.randomUUID();

        User customer = new User(); customer.setId(customerId);
        CustomerProfile custProfile = new CustomerProfile(); custProfile.setUser(customer);

        User workerUser = new User(); workerUser.setId(UUID.randomUUID());
        WorkerProfile worker = new WorkerProfile(); worker.setUser(workerUser);

        Deal deal = new Deal();
        deal.setId(dealId);
        deal.setCustomer(custProfile);
        deal.setWorker(worker);
        deal.setStatus(DealStatus.COMPLETED);

        when(dealRepository.findById(dealId)).thenReturn(Optional.of(deal));
        when(reviewRepository.save(any(Review.class))).thenAnswer(i -> {
            Review r = i.getArgument(0);
            r.setId(UUID.randomUUID());
            return r;
        });

        ReviewDto res = reviewService.create(customerId, dealId, new ReviewCreateDto(5, "good"));
        assertThat(res).isNotNull();
        assertThat(res.rating()).isEqualTo(5);

        assertThrows(RuntimeException.class, () -> reviewService.create(UUID.randomUUID(), dealId, new ReviewCreateDto(5, "bad")));
    }

    @Test
    void listByWorkerRetrievesAndMaps() {
        UUID workerId = UUID.randomUUID();

        WorkerProfile worker = new WorkerProfile(); worker.setId(UUID.randomUUID());
        User workerUser = new User(); workerUser.setId(UUID.randomUUID()); worker.setUser(workerUser);
        when(workerProfileRepository.findByUserId(workerId)).thenReturn(Optional.of(worker));
        Review review = new Review(); review.setId(UUID.randomUUID()); review.setTargetWorker(worker);

        Deal deal = new Deal();
        deal.setId(UUID.randomUUID());
        review.setDeal(deal);
        review.setAuthorUser(new User());
        review.setRating(4);

        when(reviewRepository.findAllByTargetWorker(worker)).thenReturn(List.of(review));

        var list = reviewService.listByWorker(workerId);
        assertThat(list).hasSize(1);
    }
}
