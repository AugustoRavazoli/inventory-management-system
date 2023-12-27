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

    public Page<Product> listProducts(Pageable page) {
        return productRepository.findAll(page);
    }

}
