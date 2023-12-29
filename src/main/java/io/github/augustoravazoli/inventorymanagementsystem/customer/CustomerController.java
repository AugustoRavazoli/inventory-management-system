package io.github.augustoravazoli.inventorymanagementsystem.customer;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/list")
    public String listCustomers(@RequestParam(name = "name", defaultValue = "") String name, Pageable pageable, Model model) {
        var customerPage = customerService.listCustomers(name, pageable);
        model.addAttribute("customers", customerPage.getContent());
        model.addAttribute("currentPage", customerPage.getNumber() + 1);
        model.addAttribute("totalPages", customerPage.getTotalPages());
        return "customer/customer-table";
    }

}
