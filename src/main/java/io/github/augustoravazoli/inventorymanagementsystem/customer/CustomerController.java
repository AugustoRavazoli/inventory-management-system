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
        model.addAttribute("mode", "create");
        return "customer/customer-form";
    }

    @PostMapping("/create")
    public String createCustomer(@Valid @ModelAttribute CustomerForm customer, Model model) {
        try {
            customerService.createCustomer(customer.toEntity());
        } catch (CustomerNameTakenException e) {
            model.addAttribute("duplicatedName", true);
            model.addAttribute("customer", customer);
            model.addAttribute("mode", "create");
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

    @GetMapping("/update/{id}")
    public String retrieveUpdateCustomerPage(@PathVariable("id") long id, Model model) {
        var customer = customerService.findCustomer(id);
        model.addAttribute("customer", customer.toForm());
        model.addAttribute("id", customer.getId());
        model.addAttribute("mode", "update");
        return "customer/customer-form";
    }

    @PostMapping("/update/{id}")
    public String updateCustomer(@PathVariable("id") long id, @Valid @ModelAttribute CustomerForm customer, Model model) {
        try {
            customerService.updateCustomer(id, customer.toEntity());
        } catch (CustomerNameTakenException e) {
            model.addAttribute("duplicatedName", true);
            model.addAttribute("customer", customer);
            model.addAttribute("id", id);
            model.addAttribute("mode", "update");
            return "customer/customer-form";
        }
        return "redirect:/customers/list";
    }

}
