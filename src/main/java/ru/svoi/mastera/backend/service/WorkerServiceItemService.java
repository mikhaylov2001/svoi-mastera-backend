package ru.svoi.mastera.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.svoi.mastera.backend.dto.UpsertWorkerServiceItemDto;
import ru.svoi.mastera.backend.dto.WorkerServiceItemDto;
import ru.svoi.mastera.backend.entity.Category;  // ✅ ДОБАВЛЕНО
import ru.svoi.mastera.backend.entity.User;
import ru.svoi.mastera.backend.entity.WorkerProfile;
import ru.svoi.mastera.backend.entity.WorkerServiceItem;
import ru.svoi.mastera.backend.repository.CategoryRepository;  // ✅ ДОБАВЛЕНО
import ru.svoi.mastera.backend.repository.UserRepository;
import ru.svoi.mastera.backend.repository.WorkerProfileRepository;
import ru.svoi.mastera.backend.repository.WorkerServiceItemRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkerServiceItemService {

    private final WorkerServiceItemRepository workerServiceItemRepository;
    private final WorkerProfileRepository workerProfileRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;  // ✅ ДОБАВЛЕНО

    @Transactional(readOnly = true)
    public List<WorkerServiceItemDto> listMy(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        WorkerProfile worker = workerProfileRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Worker profile not found"));
        return workerServiceItemRepository.findAllByWorkerProfileOrderByCreatedAtDesc(worker)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<WorkerServiceItemDto> listAll(String query) {
        if (query == null || query.trim().isEmpty()) {
            return workerServiceItemRepository.findAllByActiveTrueOrderByCreatedAtDesc()
                    .stream().map(this::toDto).collect(Collectors.toList());
        }
        String q = query.trim();
        return workerServiceItemRepository
                .findAllByActiveTrueAndTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrderByCreatedAtDesc(q, q)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<WorkerServiceItemDto> listByWorker(UUID workerUserId) {
        WorkerProfile worker = workerProfileRepository.findByUserId(workerUserId)
                .orElseThrow(() -> new RuntimeException("Worker profile not found"));
        return workerServiceItemRepository.findAllByWorkerProfileAndActiveTrueOrderByCreatedAtDesc(worker)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public WorkerServiceItemDto create(UUID userId, UpsertWorkerServiceItemDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        WorkerProfile worker = workerProfileRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Worker profile not found"));

        String title = dto != null && dto.getTitle() != null ? dto.getTitle().trim() : "";
        if (title.isEmpty()) throw new RuntimeException("Title is required");

        // ✅ ДОБАВЛЕНО: categoryId обязателен
        if (dto == null || dto.getCategoryId() == null) {
            throw new RuntimeException("Category is required");
        }

        WorkerServiceItem item = new WorkerServiceItem();
        item.setWorkerProfile(worker);
        item.setTitle(title);
        item.setDescription(dto.getDescription());
        item.setPriceFrom(dto.getPriceFrom());
        item.setPriceTo(dto.getPriceTo());
        item.setActive(dto.getActive() == null || dto.getActive());

        // Устанавливаем категорию (обязательно)
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
        item.setCategory(category);

        item = workerServiceItemRepository.save(item);
        return toDto(item);
    }

    @Transactional
    public WorkerServiceItemDto update(UUID userId, UUID itemId, UpsertWorkerServiceItemDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        WorkerProfile worker = workerProfileRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Worker profile not found"));

        WorkerServiceItem item = workerServiceItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Service item not found"));

        if (!item.getWorkerProfile().getId().equals(worker.getId())) {
            throw new RuntimeException("You are not owner of this service item");
        }

        if (dto != null && dto.getTitle() != null) {
            String title = dto.getTitle().trim();
            if (title.isEmpty()) throw new RuntimeException("Title is required");
            item.setTitle(title);
        }
        if (dto != null && dto.getDescription() != null) item.setDescription(dto.getDescription());
        if (dto != null && dto.getPriceFrom() != null) item.setPriceFrom(dto.getPriceFrom());
        if (dto != null && dto.getPriceTo() != null) item.setPriceTo(dto.getPriceTo());
        if (dto != null && dto.getActive() != null) item.setActive(dto.getActive());

        // ✅ ДОБАВЛЕНО: Обновляем категорию если указана
        if (dto != null && dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            item.setCategory(category);
        }

        item = workerServiceItemRepository.save(item);
        return toDto(item);
    }

    @Transactional
    public void delete(UUID userId, UUID itemId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        WorkerProfile worker = workerProfileRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Worker profile not found"));

        WorkerServiceItem item = workerServiceItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Service item not found"));

        if (!item.getWorkerProfile().getId().equals(worker.getId())) {
            throw new RuntimeException("You are not owner of this service item");
        }

        workerServiceItemRepository.delete(item);
    }

    private WorkerServiceItemDto toDto(WorkerServiceItem item) {
        var worker = item.getWorkerProfile();
        var user = worker.getUser();
        var name = worker.getDisplayName();
        if (name == null || name.isBlank()) {
            name = user == null ? "" : user.getEmail();
        }

        return new WorkerServiceItemDto(
                item.getId(),
                user == null ? null : user.getId(),
                name,
                item.getTitle(),
                item.getDescription(),
                item.getPriceFrom(),
                item.getPriceTo(),
                item.isActive(),
                item.getCreatedAt(),
                item.getCategory() != null ? item.getCategory().getId() : null  // ✅ ДОБАВЛЕНО
        );
    }
}