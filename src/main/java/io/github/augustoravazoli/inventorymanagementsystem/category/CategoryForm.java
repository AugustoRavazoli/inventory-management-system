package io.github.augustoravazoli.inventorymanagementsystem.category;

import jakarta.validation.constraints.NotBlank;

public class CategoryForm {

    @NotBlank
    private String name;

    public CategoryForm() {}

    public CategoryForm(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Category toEntity() {
        return new Category(name);
    }

}
