package ru.svoi.mastera.backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.svoi.mastera.backend.entity.Category;
import ru.svoi.mastera.backend.repository.CategoryRepository;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    @Override
    public void run(String... args) {
        if (categoryRepository.count() == 0) {
            Category c1 = new Category();
            c1.setName("Электрика");
            c1.setSlug("elektrika");
            c1.setActive(true);

            Category c2 = new Category();
            c2.setName("Сантехника");
            c2.setSlug("santehnika");
            c2.setActive(true);

            categoryRepository.save(c1);
            categoryRepository.save(c2);
        }
    }
}
