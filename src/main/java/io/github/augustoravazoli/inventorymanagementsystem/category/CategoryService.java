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

    public Category findCategory(long id) {
        return categoryRepository.findById(id)
                .orElseThrow(CategoryNotFoundException::new);
    }

    public void updateCategory(long id, Category updatedCategory) {
        var category = categoryRepository.findById(id)
                .orElseThrow(CategoryNotFoundException::new);
        if (!category.getName().equals(updatedCategory.getName())
            && categoryRepository.existsByName(updatedCategory.getName())) {
            throw new CategoryNameTakenException();
        }
        category.setName(updatedCategory.getName());
        categoryRepository.save(category);
    }

}
