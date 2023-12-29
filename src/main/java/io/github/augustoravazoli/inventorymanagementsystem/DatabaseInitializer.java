package io.github.augustoravazoli.inventorymanagementsystem;

import io.github.augustoravazoli.inventorymanagementsystem.category.Category;
import io.github.augustoravazoli.inventorymanagementsystem.category.CategoryRepository;
import io.github.augustoravazoli.inventorymanagementsystem.customer.Customer;
import io.github.augustoravazoli.inventorymanagementsystem.customer.CustomerRepository;
import io.github.augustoravazoli.inventorymanagementsystem.order.Order;
import io.github.augustoravazoli.inventorymanagementsystem.order.OrderItem;
import io.github.augustoravazoli.inventorymanagementsystem.order.OrderRepository;
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

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Override
    public void run(String... args) throws Exception {
        categoryRepository.saveAll(List.of(
                new Category("A"),
                new Category("B"),
                new Category("C"),
                new Category("D"),
                new Category("E"),
                new Category("F"),
                new Category("G"),
                new Category("H")
        ));
        productRepository.saveAll(List.of(
                new Product("A", new Category(1L), 50, "100"),
                new Product("B", new Category(2L),100, "150"),
                new Product("C", new Category(3L),150, "200"),
                new Product("D", new Category(4L),200, "250"),
                new Product("E", new Category(5L),250, "300"),
                new Product("F", new Category(6L),300, "350"),
                new Product("G", new Category(7L),350, "400"),
                new Product("H", new Category(8L),400, "450"),
                new Product("I", new Category(1L),450, "500"),
                new Product("J", new Category(2L),500, "550"),
                new Product("K", new Category(3L),550, "600"),
                new Product("L", new Category(4L),600, "650"),
                new Product("M", new Category(5L),650, "700"),
                new Product("N", new Category(6L),700, "750"),
                new Product("O", new Category(7L),750, "800"),
                new Product("P", new Category(8L),800, "850"),
                new Product("Q", new Category(1L),850, "900"),
                new Product("R", new Category(2L),900, "950"),
                new Product("S", new Category(3L),950, "1000"),
                new Product("T", new Category(4L),1000, "1050")
        ));
        customerRepository.saveAll(List.of(
                new Customer("A", "A", "A"),
                new Customer("B", "B", "B"),
                new Customer("C", "C", "C"),
                new Customer("D", "D", "D"),
                new Customer("E", "E", "E"),
                new Customer("F", "F", "F"),
                new Customer("G", "G", "G"),
                new Customer("H", "H", "H")
        ));
        orderRepository.saveAll(List.of(
                new Order(
                        Order.Status.UNPAID,
                        new Customer(1L,"", "", ""),
                        List.of(
                                new OrderItem(1, new Product(1L, "", null, null, null)),
                                new OrderItem(2, new Product(2L, "", null, null, null)),
                                new OrderItem(3, new Product(3L, "", null, null, null))
                        )
                ),
                new Order(
                        Order.Status.UNPAID,
                        new Customer(2L,"", "", ""),
                        List.of(
                                new OrderItem(4, new Product(1L, "", null, null, null)),
                                new OrderItem(5, new Product(2L, "", null, null, null)),
                                new OrderItem(6, new Product(3L, "", null, null, null))
                        )
                ),                new Order(
                        Order.Status.UNPAID,
                        new Customer(3L,"", "", ""),
                        List.of(
                                new OrderItem(7, new Product(1L, "", null, null, null)),
                                new OrderItem(8, new Product(2L, "", null, null, null)),
                                new OrderItem(9, new Product(3L, "", null, null, null))
                        )
                ),                new Order(
                        Order.Status.UNPAID,
                        new Customer(4L,"", "", ""),
                        List.of(
                                new OrderItem(10, new Product(1L, "", null, null, null)),
                                new OrderItem(11, new Product(2L, "", null, null, null)),
                                new OrderItem(12, new Product(3L, "", null, null, null))
                        )
                )
        ));
    }

}
