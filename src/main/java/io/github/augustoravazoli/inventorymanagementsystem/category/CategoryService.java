package io.github.augustoravazoli.inventorymanagementsystem.category;

import io.github.augustoravazoli.inventorymanagementsystem.user.User;
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

    public void createCategory(Category category, User owner) {
        if (categoryRepository.existsByNameAndOwner(category.getName(), owner)) {
            logger.info("Category name {} for user {} already in use, throwing exception", category.getName(), owner.getEmail());
            throw new CategoryNameTakenException();
        }
        category.setOwner(owner);
        categoryRepository.save(category);
        logger.info("Category {} created for user {}", category.getName(), owner.getEmail());
    }

    public Page<Category> listCategories(int page, User owner) {
        logger.info("Listing categories paginated for user {}", owner.getEmail());
        return categoryRepository.findAllByOwner(owner, PageRequest.of(page - 1, 8, Sort.by("name")));
    }

    public List<Category> listCategories(User owner) {
        logger.info("Listing categories for user {}", owner.getEmail());
        return categoryRepository.findAllByOwner(owner, Sort.by("name"));
    }

    public List<Category> findCategories(String name, User owner) {
        logger.info("Finding categories containing name {} for user {}", name, owner.getEmail());
        return categoryRepository.findAllByNameContainingIgnoreCaseAndOwner(name, owner);
    }

    public Category findCategory(long id, User owner) {
        logger.info("Finding category with id {} for user {}", id, owner.getEmail());
        return categoryRepository.findByIdAndOwner(id, owner)
                .orElseThrow(CategoryNotFoundException::new);
    }

    public void updateCategory(long id, Category updatedCategory, User owner) {
        var category = categoryRepository.findByIdAndOwner(id, owner)
                .orElseThrow(CategoryNotFoundException::new);
        if (!category.getName().equals(updatedCategory.getName())
            && categoryRepository.existsByNameAndOwner(updatedCategory.getName(), owner)) {
            logger.info("New category name {} of user {} already in use, throwing exception", updatedCategory.getName(), owner.getEmail());
            throw new CategoryNameTakenException();
        }
        category.setName(updatedCategory.getName());
        categoryRepository.save(category);
        logger.info("Category with id {} of user {} updated, new name is {}", category.getId(), owner.getEmail(), updatedCategory.getName());
    }

    public void deleteCategory(long id, User owner) {
        if (!categoryRepository.existsByIdAndOwner(id, owner)) {
            logger.info("Category with id {} of user user {} not found, throwing exception", id, owner.getEmail());
            throw new CategoryNotFoundException();
        }
        categoryRepository.deleteById(id);
        logger.info("Category with id {} of user {} deleted", id, owner.getEmail());
    }

}
