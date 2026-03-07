package ru.svoi.mastera.backend.dto;

import java.util.UUID;

public class CategoryDto {

    private UUID id;
    private String name;
    private String slug;

    public CategoryDto() {
    }

    public CategoryDto(UUID id, String name, String slug) {
        this.id = id;
        this.name = name;
        this.slug = slug;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }
}
