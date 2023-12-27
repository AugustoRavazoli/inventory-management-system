package io.github.augustoravazoli.inventorymanagementsystem.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public void createProduct(Product product) {
        if (productRepository.existsByName(product.getName())) {
            throw new ProductNameTakenException();
        }
        productRepository.save(product);
    }

    public Page<Product> listProducts(Pageable page) {
        return productRepository.findAll(page);
    }

}
