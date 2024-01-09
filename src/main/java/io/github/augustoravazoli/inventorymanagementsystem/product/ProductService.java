package io.github.augustoravazoli.inventorymanagementsystem.product;

import io.github.augustoravazoli.inventorymanagementsystem.category.CategoryRepository;
import io.github.augustoravazoli.inventorymanagementsystem.order.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final OrderRepository orderRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository,
                          OrderRepository orderRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.orderRepository = orderRepository;
    }

    public void createProduct(Product product) {
        if (productRepository.existsByName(product.getName())) {
            throw new ProductNameTakenException();
        }
        if (product.getCategory() != null
            && !categoryRepository.existsById(product.getCategory().getId())) {
            throw new InvalidCategoryException();
        }
        productRepository.save(product);
    }

    public Page<Product> listProducts(int page) {
        return productRepository.findAll(PageRequest.of(page - 1, 8, Sort.by("name")));
    }

    public List<Product> listProducts() {
        return productRepository.findAll(Sort.by("name"));
    }

    public List<Product> findProducts(String name) {
        return productRepository.findAllByNameContainingIgnoreCase(name);
    }

    public Product findProduct(long id) {
        return productRepository.findById(id)
                .orElseThrow(ProductNotFoundException::new);
    }

    public void updateProduct(long id, Product updatedProduct) {
        var product = productRepository.findById(id)
                .orElseThrow(ProductNotFoundException::new);
        if (!product.getName().equals(updatedProduct.getName())
            && productRepository.existsByName(updatedProduct.getName())) {
            throw new ProductNameTakenException();
        }
        if (updatedProduct.getCategory() != null
            && !categoryRepository.existsById(updatedProduct.getCategory().getId())) {
            throw new InvalidCategoryException();
        }
        product.setName(updatedProduct.getName());
        product.setCategory(updatedProduct.getCategory());
        product.setQuantity(updatedProduct.getQuantity());
        product.setPrice(updatedProduct.getPrice());
        productRepository.save(product);
    }

    public void deleteProduct(long id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException();
        }
        if (orderRepository.existsByItemsProductId(id)) {
            throw new ProductDeletionNotAllowedException();
        }
        productRepository.deleteById(id);
    }

}
