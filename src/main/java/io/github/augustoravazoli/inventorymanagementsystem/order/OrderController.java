package io.github.augustoravazoli.inventorymanagementsystem.order;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
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
