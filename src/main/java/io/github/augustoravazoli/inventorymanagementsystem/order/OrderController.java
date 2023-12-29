package io.github.augustoravazoli.inventorymanagementsystem.order;

import io.github.augustoravazoli.inventorymanagementsystem.customer.CustomerService;
import io.github.augustoravazoli.inventorymanagementsystem.product.ProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final ProductService productService;
    private final CustomerService customerService;

    public OrderController(OrderService orderService, ProductService productService, CustomerService customerService) {
        this.orderService = orderService;
        this.productService = productService;
        this.customerService = customerService;
    }

    @GetMapping("/create")
    public String retrieveCreateOrderPage(Model model) {
        model.addAttribute("order", new OrderForm());
        model.addAttribute("customers", customerService.listCustomers());
        model.addAttribute("products", productService.listProducts());
        return "order/order-form";
    }

    @PostMapping("/create")
    public String createOrder(@Valid @ModelAttribute OrderForm order, Model model) {
        try {
            orderService.createOrder(order.toEntity());
        } catch (ProductWithInsufficientStockException e) {
            model.addAttribute("insufficientStock", true);
            model.addAttribute("order", new OrderForm());
            model.addAttribute("customer", customerService.listCustomers());
            model.addAttribute("products", productService.listProducts());
            return "order/order-form";
        }
        return "redirect:/orders/list";
    }

    @GetMapping("/list")
    public String listOrders(@RequestParam(name = "customer-name", defaultValue = "") String customerName, Pageable pageable, Model model) {
        var orderPage = orderService.listOrders(customerName, pageable);
        model.addAttribute("orders", orderPage.getContent());
        model.addAttribute("currentPage", orderPage.getNumber() + 1);
        model.addAttribute("totalPages", orderPage.getTotalPages());
        return "order/order-table";
    }

}
