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
        model.addAttribute("mode", "create");
        return "order/order-form";
    }

    @PostMapping("/create")
    public String createOrder(@Valid @ModelAttribute OrderForm order, Model model) {
        try {
            orderService.createOrder(order.toEntity());
            return "redirect:/orders/list";
        } catch (ProductWithInsufficientStockException e) {
            model.addAttribute("insufficientStock", true);
        } catch (DuplicatedOrderItemException e) {
            model.addAttribute("duplicatedItem", true);
        }
        model.addAttribute("order", order);
        model.addAttribute("customers", customerService.listCustomers());
        model.addAttribute("products", productService.listProducts());
        model.addAttribute("mode", "create");
        return "order/order-form";
    }

    @GetMapping("/list")
    public String listOrders(@RequestParam(name = "customer-name", defaultValue = "") String customerName, Pageable pageable, Model model) {
        var orderPage = orderService.listOrders(customerName, pageable);
        model.addAttribute("orders", orderPage.getContent());
        model.addAttribute("currentPage", orderPage.getNumber() + 1);
        model.addAttribute("totalPages", orderPage.getTotalPages());
        return "order/order-table";
    }

    @GetMapping("/update/{id}")
    public String retrieveUpdateOrderPage(@PathVariable("id") long id, Model model) {
        var order = orderService.findOrder(id);
        model.addAttribute("order", order.toForm());
        model.addAttribute("id", order.getId());
        model.addAttribute("customers", customerService.listCustomers());
        model.addAttribute("products", productService.listProducts());
        model.addAttribute("mode", "update");
        return "order/order-form";
    }

    @PostMapping("/update/{id}")
    public String updateOrder(@PathVariable("id") long id, @Valid @ModelAttribute OrderForm order, Model model) {
        try {
            orderService.updateOrder(id, order.toEntity());
            return "redirect:/orders/list";
        } catch (ProductWithInsufficientStockException e) {
            model.addAttribute("insufficientStock", true);
        } catch (DuplicatedOrderItemException e) {
            model.addAttribute("duplicatedItem", true);
        }
        model.addAttribute("order", order);
        model.addAttribute("id", id);
        model.addAttribute("customers", customerService.listCustomers());
        model.addAttribute("products", productService.listProducts());
        model.addAttribute("mode", "update");
        return "order/order-form";
    }

}
