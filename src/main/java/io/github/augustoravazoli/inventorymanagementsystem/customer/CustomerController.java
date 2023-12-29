package io.github.augustoravazoli.inventorymanagementsystem.customer;

import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/create")
    public String retrieveCreateCustomerPage(Model model) {
        model.addAttribute("customer", new CustomerForm());
        return "customer/customer-form";
    }

    @PostMapping("/create")
    public String createCustomer(@Valid @ModelAttribute CustomerForm customer, Model model) {
        try {
            customerService.createCustomer(customer.toEntity());
        } catch (CustomerNameTakenException e) {
            model.addAttribute("duplicatedName", true);
            model.addAttribute("customer", customer);
            return "customer/customer-form";
        }
        return "redirect:/customers/list";
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
