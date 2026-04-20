package ru.svoi.mastera.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.svoi.mastera.backend.dto.ListingCreateDto;
import ru.svoi.mastera.backend.dto.ListingDto;
import ru.svoi.mastera.backend.entity.Listing;
import ru.svoi.mastera.backend.entity.WorkerProfile;
import ru.svoi.mastera.backend.repository.ListingRepository;
import ru.svoi.mastera.backend.repository.WorkerProfileRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListingService {

    private final ListingRepository listingRepository;
    private final WorkerProfileRepository workerProfileRepository;

    @Transactional
    public ListingDto create(UUID workerUserId, ListingCreateDto dto) {
        WorkerProfile worker = workerProfileRepository.findByUserId(workerUserId)
                .orElseThrow(() -> new RuntimeException("Worker profile not found"));

        Listing listing = new Listing();
        listing.setWorker(worker);
        listing.setTitle(dto.title());
        listing.setDescription(dto.description());
        listing.setPrice(dto.price());
        listing.setPriceUnit(dto.priceUnit());
        listing.setCategory(dto.category());
        listing.setPhotos(dto.photos());
        listing.setActive(true);

        return toDto(listingRepository.save(listing));
    }

    @Transactional
    public ListingDto update(UUID workerUserId, UUID listingId, ListingCreateDto dto) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new RuntimeException("Listing not found"));

        if (!listing.getWorker().getUser().getId().equals(workerUserId)) {
            throw new RuntimeException("Not your listing");
        }

        listing.setTitle(dto.title());
        listing.setDescription(dto.description());
        listing.setPrice(dto.price());
        listing.setPriceUnit(dto.priceUnit());
        listing.setCategory(dto.category());
        listing.setPhotos(dto.photos());

        return toDto(listingRepository.save(listing));
    }

    @Transactional
    public void delete(UUID workerUserId, UUID listingId) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new RuntimeException("Listing not found"));

        if (!listing.getWorker().getUser().getId().equals(workerUserId)) {
            throw new RuntimeException("Not your listing");
        }

        listing.setActive(false);
        listingRepository.save(listing);
    }

    @Transactional
    public ListingDto restore(UUID workerUserId, UUID listingId) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new RuntimeException("Listing not found"));

        if (!listing.getWorker().getUser().getId().equals(workerUserId)) {
            throw new RuntimeException("Not your listing");
        }

        listing.setActive(true);
        return toDto(listingRepository.save(listing));
    }

    @Transactional(readOnly = true)
    public List<ListingDto> getByWorker(UUID workerUserId) {
        WorkerProfile worker = workerProfileRepository.findByUserId(workerUserId)
                .orElseThrow(() -> new RuntimeException("Worker profile not found"));
        return listingRepository.findAllByWorkerOrderByCreatedAtDesc(worker)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ListingDto> getAll() {
        return listingRepository.findAllActiveOrderByCreatedAtDesc()
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ListingDto getById(UUID listingId) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new RuntimeException("Listing not found"));
        return toDto(listing);
    }

    private ListingDto toDto(Listing l) {
        String workerAvatar = l.getWorker().getUser() != null
                ? l.getWorker().getUser().getAvatarUrl() : null;
        return new ListingDto(
                l.getId(),
                l.getWorker().getUser().getId(),
                l.getWorker().getDisplayName(),
                l.getWorker().getLastName(),
                workerAvatar,
                l.getTitle(),
                l.getDescription(),
                l.getPrice(),
                l.getPriceUnit(),
                l.getCategory(),
                l.getPhotos(),
                l.isActive(),
                l.getCreatedAt()
        );
    }
}