package io.github.augustoravazoli.inventorymanagementsystem.category;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public Page<Category> listCategories(int page) {
        return categoryRepository.findAll(PageRequest.of(page - 1, 8, Sort.by("name")));
    }

    public List<Category> listCategories() {
        return categoryRepository.findAll(Sort.by("name"));
    }

    public List<Category> findCategories(String name) {
        return categoryRepository.findAllByNameContainingIgnoreCase(name);
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

    public void deleteCategory(long id) {
        if (!categoryRepository.existsById(id)) {
            throw new CategoryNotFoundException();
        }
        categoryRepository.deleteById(id);
    }

}
