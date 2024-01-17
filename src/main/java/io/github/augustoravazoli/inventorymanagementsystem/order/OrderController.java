package io.github.augustoravazoli.inventorymanagementsystem.order;

import io.github.augustoravazoli.inventorymanagementsystem.customer.CustomerService;
import io.github.augustoravazoli.inventorymanagementsystem.product.ProductService;
import io.github.augustoravazoli.inventorymanagementsystem.user.User;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    public String retrieveCreateOrderPage(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("order", new OrderForm());
        model.addAttribute("customers", customerService.listCustomers(user));
        model.addAttribute("products", productService.listProducts(user));
        model.addAttribute("mode", "create");
        return "order/order-form";
    }

    @PostMapping("/create")
    public String createOrder(@AuthenticationPrincipal User user, @Valid @ModelAttribute OrderForm order, Model model) {
        try {
            orderService.createOrder(order.toEntity(), user);
            return "redirect:/orders/list?status=UNPAID";
        } catch (ProductWithInsufficientStockException e) {
            model.addAttribute("insufficientStock", true);
        } catch (DuplicatedOrderItemException e) {
            model.addAttribute("duplicatedItem", true);
        }
        model.addAttribute("order", order);
        model.addAttribute("customers", customerService.listCustomers(user));
        model.addAttribute("products", productService.listProducts(user));
        model.addAttribute("mode", "create");
        return "order/order-form";
    }

    @GetMapping("/list")
    public String listOrders(
            @AuthenticationPrincipal User user,
            @RequestParam("status") OrderStatus status,
            @RequestParam(name = "page", defaultValue = "1") int page,
            Model model,
            HttpSession session
    ) {
        var orderPage = orderService.listOrders(status, page, user);
        model.addAttribute("orders", orderPage.getContent());
        model.addAttribute("currentPage", orderPage.getNumber() + 1);
        model.addAttribute("totalPages", orderPage.getTotalPages());
        session.setAttribute("status", status);
        return "order/order-table";
    }

    @GetMapping("/find")
    public String findOrders(
            @AuthenticationPrincipal User user,
            @RequestParam("status") OrderStatus status,
            @RequestParam("customer-name") String customerName,
            Model model
    ) {
        var orders = orderService.findOrders(status, customerName, user);
        model.addAttribute("orders", orders);
        return "order/order-table";
    }

    @GetMapping("/update/{id}")
    public String retrieveUpdateOrderPage(@AuthenticationPrincipal User user, @PathVariable("id") long id, Model model) {
        var order = orderService.findOrder(id, user);
        model.addAttribute("order", order.toForm());
        model.addAttribute("id", order.getId());
        model.addAttribute("customers", customerService.listCustomers(user));
        model.addAttribute("products", productService.listProducts(user));
        model.addAttribute("mode", "update");
        return "order/order-form";
    }

    @PostMapping("/update/{id}")
    public String updateOrder(
            @AuthenticationPrincipal User user,
            @PathVariable("id") long id,
            @Valid @ModelAttribute OrderForm order,
            Model model,
            RedirectAttributes redirectAttributes,
            HttpSession session
    ) {
        try {
            orderService.updateOrder(id, order.toEntity(), user);
            redirectAttributes.addAttribute("status", session.getAttribute("status"));
            return "redirect:/orders/list";
        } catch (ProductWithInsufficientStockException e) {
            model.addAttribute("insufficientStock", true);
        } catch (DuplicatedOrderItemException e) {
            model.addAttribute("duplicatedItem", true);
        }
        model.addAttribute("order", order);
        model.addAttribute("id", id);
        model.addAttribute("customers", customerService.listCustomers(user));
        model.addAttribute("products", productService.listProducts(user));
        model.addAttribute("mode", "update");
        return "order/order-form";
    }

    @PostMapping("/delete/{id}")
    public String deleteOrder(@AuthenticationPrincipal User user, @PathVariable("id") long id, RedirectAttributes redirectAttributes, HttpSession session) {
        orderService.deleteOrder(id, user);
        redirectAttributes.addAttribute("status", session.getAttribute("status"));
        return "redirect:/orders/list";
    }

}
