package io.github.augustoravazoli.inventorymanagementsystem.product;

import io.github.augustoravazoli.inventorymanagementsystem.category.CategoryRepository;
import io.github.augustoravazoli.inventorymanagementsystem.order.OrderRepository;
import io.github.augustoravazoli.inventorymanagementsystem.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final OrderRepository orderRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository,
                          OrderRepository orderRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.orderRepository = orderRepository;
    }

    public void createProduct(Product product, User owner) {
        if (productRepository.existsByNameAndOwner(product.getName(), owner)) {
            logger.info("Product name {} of user {} already in use, throwing exception", product.getName(), owner.getEmail());
            throw new ProductNameTakenException();
        }
        if (product.getCategory() != null
            && !categoryRepository.existsByIdAndOwner(product.getCategory().getId(), owner)) {
            logger.info("Product category with id {} not found, throwing exception", product.getCategory().getId());
            throw new InvalidCategoryException();
        }
        product.setOwner(owner);
        productRepository.save(product);
        logger.info("Product {} created for user {}", product.getName(), owner.getEmail());
    }

    public Page<Product> listProducts(int page, User owner) {
        logger.info("Listing products paginated for user {}", owner.getEmail());
        return productRepository.findAllByOwner(owner, PageRequest.of(page - 1, 8, Sort.by("name")));
    }

    public List<Product> listProducts(User owner) {
        logger.info("Listing products for user {}", owner.getEmail());
        return productRepository.findAllByOwner(owner, Sort.by("name"));
    }

    public List<Product> findProducts(String name, User owner) {
        logger.info("Finding products containing name {} for user {}", name, owner.getEmail());
        return productRepository.findAllByNameContainingIgnoreCaseAndOwner(name, owner);
    }

    public Product findProduct(long id, User owner) {
        logger.info("Finding product with id {} for user {}", id, owner.getEmail());
        return productRepository.findByIdAndOwner(id, owner)
                .orElseThrow(ProductNotFoundException::new);
    }

    public void updateProduct(long id, Product updatedProduct, User owner) {
        var product = productRepository.findByIdAndOwner(id, owner)
                .orElseThrow(ProductNotFoundException::new);
        if (!product.getName().equals(updatedProduct.getName())
            && productRepository.existsByNameAndOwner(updatedProduct.getName(), owner)) {
            logger.info("New product name {} of user {} already in use, throwing exception", updatedProduct.getName(), owner.getEmail());
            throw new ProductNameTakenException();
        }
        if (updatedProduct.getCategory() != null
            && !categoryRepository.existsByIdAndOwner(updatedProduct.getCategory().getId(), owner)) {
            logger.info("New product category with id {} of user {} not found, throwing exception", updatedProduct.getCategory().getId(), owner.getEmail());
            throw new InvalidCategoryException();
        }
        product.setName(updatedProduct.getName());
        product.setCategory(updatedProduct.getCategory());
        product.setQuantity(updatedProduct.getQuantity());
        product.setPrice(updatedProduct.getPrice());
        productRepository.save(product);
        logger.info("Product {} of user {} updated, new name is {}", product.getName(), owner.getEmail(), updatedProduct.getName());
    }

    public void deleteProduct(long id, User owner) {
        if (!productRepository.existsByIdAndOwner(id, owner)) {
            logger.info("Product with id {} of user {} not found, throwing exception", id, owner.getEmail());
            throw new ProductNotFoundException();
        }
        if (orderRepository.existsByItemsProductIdAndOwner(id, owner)) {
            logger.info("Product with id {} of user {} still is being used in orders, throwing exception", id, owner.getEmail());
            throw new ProductDeletionNotAllowedException();
        }
        productRepository.deleteById(id);
        logger.info("Product with id {} of user {} deleted", id, owner.getEmail());
    }

}
