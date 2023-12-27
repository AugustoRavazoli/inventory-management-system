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
        product.setName(updatedProduct.getName());
        product.setQuantity(updatedProduct.getQuantity());
        product.setPrice(updatedProduct.getPrice());
        productRepository.save(product);
    }

    public void deleteProduct(long id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException();
        }
        productRepository.deleteById(id);
    }

}
