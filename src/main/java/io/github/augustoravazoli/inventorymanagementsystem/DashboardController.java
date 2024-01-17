package io.github.augustoravazoli.inventorymanagementsystem;

import io.github.augustoravazoli.inventorymanagementsystem.category.CategoryRepository;
import io.github.augustoravazoli.inventorymanagementsystem.customer.CustomerRepository;
import io.github.augustoravazoli.inventorymanagementsystem.order.Order;
import io.github.augustoravazoli.inventorymanagementsystem.order.OrderRepository;
import io.github.augustoravazoli.inventorymanagementsystem.order.OrderStatus;
import io.github.augustoravazoli.inventorymanagementsystem.product.ProductRepository;
import io.github.augustoravazoli.inventorymanagementsystem.user.User;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final CustomerRepository customerRepository;

    public DashboardController(OrderRepository orderRepository, ProductRepository productRepository, CategoryRepository categoryRepository, CustomerRepository customerRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.customerRepository = customerRepository;
    }

    @GetMapping
    public String retrieveDashboardPage(@AuthenticationPrincipal User user, Model model) {
        var totalSales = orderRepository.findAllByStatusAndOwner(OrderStatus.PAID, user, Pageable.unpaged())
                .stream()
                .map(Order::getAmount)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
        model.addAttribute("totalCustomers", customerRepository.countByOwner(user));
        model.addAttribute("totalCategories", categoryRepository.countByOwner(user));
        model.addAttribute("totalProducts", productRepository.countByOwner(user));
        model.addAttribute("totalUnpaidOrders", orderRepository.countByStatusAndOwner(OrderStatus.UNPAID, user));
        model.addAttribute("totalPaidOrders", orderRepository.countByStatusAndOwner(OrderStatus.PAID, user));
        model.addAttribute("totalSales", totalSales);
        return "dashboard";
    }

}
