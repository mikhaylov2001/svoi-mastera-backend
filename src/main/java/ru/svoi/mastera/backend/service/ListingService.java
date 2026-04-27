package ru.svoi.mastera.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.svoi.mastera.backend.dto.ListingCreateDto;
import ru.svoi.mastera.backend.dto.ListingDto;
import ru.svoi.mastera.backend.entity.Listing;
import ru.svoi.mastera.backend.entity.WorkerProfile;
import ru.svoi.mastera.backend.entity.enams.DealStatus;
import ru.svoi.mastera.backend.repository.DealRepository;
import ru.svoi.mastera.backend.repository.ListingRepository;
import ru.svoi.mastera.backend.repository.WorkerProfileRepository;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListingService {

    private final ListingRepository listingRepository;
    private final WorkerProfileRepository workerProfileRepository;
    private final DealRepository dealRepository;

    @Transactional
    public ListingDto create(UUID workerUserId, ListingCreateDto dto) {
        WorkerProfile worker = workerProfileRepository.findByUserId(workerUserId)
                .orElseThrow(() -> new RuntimeException("Worker profile not found"));

        Listing listing = new Listing();
        listing.setWorker(worker);
        listing.setTitle(dto.title());
        listing.setDescription(dto.description() == null ? "" : dto.description());
        listing.setPrice(dto.price());
        listing.setPriceUnit(dto.priceUnit());
        listing.setCategory(dto.category());
        listing.setPhotosJson(photosToJson(dto.photos()));
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
        listing.setDescription(dto.description() == null ? "" : dto.description());
        listing.setPrice(dto.price());
        listing.setPriceUnit(dto.priceUnit());
        listing.setCategory(dto.category());
        listing.setPhotosJson(photosToJson(dto.photos()));

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

    @Transactional
    public void recordView(UUID listingId) {
        Listing listing = listingRepository.findById(listingId).orElse(null);
        if (listing == null) {
            return;
        }
        listing.setViewCount(listing.getViewCount() + 1);
        listingRepository.save(listing);
    }

    private static String photosToJson(String[] photos) {
        if (photos == null || photos.length == 0) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < photos.length; i++) {
            sb.append('"');
            sb.append(photos[i].replace("\\", "\\\\").replace("\"", "\\\""));
            sb.append('"');
            if (i < photos.length - 1) sb.append(',');
        }
        sb.append(']');
        return sb.toString();
    }

    private static String[] photosFromJson(String json) {
        if (json == null || json.isBlank() || json.equals("[]")) return new String[0];
        String inner = json.trim();
        if (inner.startsWith("[")) inner = inner.substring(1);
        if (inner.endsWith("]")) inner = inner.substring(0, inner.length() - 1);
        if (inner.isBlank()) return new String[0];
        String[] parts = inner.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        return Arrays.stream(parts)
                .map(s -> s.trim().replaceAll("^\"|\"$", "").replace("\\\"", "\"").replace("\\\\", "\\"))
                .toArray(String[]::new);
    }

    private ListingDto toDto(Listing l) {
        long pendingDeals = dealRepository.countByListingIdAndStatus(l.getId(), DealStatus.NEW);
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
                photosFromJson(l.getPhotosJson()),
                l.isActive(),
                l.getCreatedAt(),
                l.getViewCount(),
                pendingDeals
        );
    }
}