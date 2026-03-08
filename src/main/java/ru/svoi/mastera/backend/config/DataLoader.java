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
            addCategory("Ремонт квартир", "remont-kvartir");
            addCategory("Сантехника", "santehnika");
            addCategory("Электрика", "elektrika");
            addCategory("Уборка", "uborka");
            addCategory("Парикмахер", "parikhmaher");
            addCategory("Маникюр и педикюр", "manikur");
            addCategory("Красота и здоровье", "krasota-i-zdorovie");
            addCategory("Репетиторство", "repetitorstvo");
            addCategory("Компьютерная помощь", "kompyuternaya-pomosh");
        }
    }

    private void addCategory(String name, String slug) {
        Category cat = new Category();
        cat.setName(name);
        cat.setSlug(slug);
        cat.setActive(true);
        categoryRepository.save(cat);
    }
}