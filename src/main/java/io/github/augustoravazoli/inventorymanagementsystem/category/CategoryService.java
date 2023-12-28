package io.github.augustoravazoli.inventorymanagementsystem.category;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public void createCategory(Category category) {
        if (categoryRepository.existsByName(category.getName())) {
            throw new CategoryNameTakenException();
        }
        categoryRepository.save(category);
    }

    public Page<Category> listCategories(String name, Pageable page) {
        if (name.isEmpty()) {
            return categoryRepository.findAll(page);
        }
        return categoryRepository.findAllByNameContainingIgnoreCase(name, page);
    }

}
