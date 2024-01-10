package io.github.augustoravazoli.inventorymanagementsystem.category;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private static final Logger logger = LoggerFactory.getLogger(CategoryService.class);

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public void createCategory(Category category) {
        if (categoryRepository.existsByName(category.getName())) {
            logger.info("Category name {} already in use, throwing exception", category.getName());
            throw new CategoryNameTakenException();
        }
        categoryRepository.save(category);
        logger.info("Category {} created", category.getName());
    }

    public Page<Category> listCategories(int page) {
        logger.info("Listing categories paginated");
        return categoryRepository.findAll(PageRequest.of(page - 1, 8, Sort.by("name")));
    }

    public List<Category> listCategories() {
        logger.info("Listing categories");
        return categoryRepository.findAll(Sort.by("name"));
    }

    public List<Category> findCategories(String name) {
        logger.info("Finding categories containing name {}", name);
        return categoryRepository.findAllByNameContainingIgnoreCase(name);
    }

    public Category findCategory(long id) {
        logger.info("Finding category with id {}", id);
        return categoryRepository.findById(id)
                .orElseThrow(CategoryNotFoundException::new);
    }

    public void updateCategory(long id, Category updatedCategory) {
        var category = categoryRepository.findById(id)
                .orElseThrow(CategoryNotFoundException::new);
        if (!category.getName().equals(updatedCategory.getName())
            && categoryRepository.existsByName(updatedCategory.getName())) {
            logger.info("New category name {} already in use, throwing exception", updatedCategory.getName());
            throw new CategoryNameTakenException();
        }
        category.setName(updatedCategory.getName());
        categoryRepository.save(category);
        logger.info("Category {} updated, new name is {}", category.getName(), updatedCategory.getName());
    }

    public void deleteCategory(long id) {
        if (!categoryRepository.existsById(id)) {
            logger.info("Category with id {} not found, throwing exception", id);
            throw new CategoryNotFoundException();
        }
        categoryRepository.deleteById(id);
        logger.info("Category with id {} deleted", id);
    }

}
