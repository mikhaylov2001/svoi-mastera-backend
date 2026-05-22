package ru.svoi.mastera.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.svoi.mastera.backend.entity.Category;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    List<Category> findAllByActiveTrueOrderByNameAsc();
    Optional<Category> findByNameIgnoreCase(String name);
    Optional<Category> findBySlugIgnoreCase(String slug);
}
