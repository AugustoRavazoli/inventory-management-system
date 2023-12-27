package io.github.augustoravazoli.inventorymanagementsystem;

import io.github.augustoravazoli.inventorymanagementsystem.product.Product;
import io.github.augustoravazoli.inventorymanagementsystem.product.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private ProductRepository productRepository;

    @Override
    public void run(String... args) throws Exception {
        productRepository.saveAll(List.of(
                new Product("A", 50, "100"),
                new Product("B", 100, "150"),
                new Product("C", 150, "200"),
                new Product("D", 200, "250"),
                new Product("E", 250, "300"),
                new Product("F", 300, "350"),
                new Product("G", 350, "400"),
                new Product("H", 400, "450"),
                new Product("I", 450, "500"),
                new Product("J", 500, "550"),
                new Product("K", 550, "600"),
                new Product("L", 600, "650"),
                new Product("M", 650, "700"),
                new Product("N", 700, "750"),
                new Product("O", 750, "800"),
                new Product("P", 800, "850"),
                new Product("Q", 850, "900"),
                new Product("R", 900, "950"),
                new Product("S", 950, "1000"),
                new Product("T", 1000, "1050")
        ));
    }

}
