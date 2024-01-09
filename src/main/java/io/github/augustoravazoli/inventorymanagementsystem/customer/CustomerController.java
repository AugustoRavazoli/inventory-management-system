package io.github.augustoravazoli.inventorymanagementsystem.customer;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    public String listCustomers(@RequestParam(name = "page", defaultValue = "1") int page, Model model) {
        var customerPage = customerService.listCustomers(page);
        model.addAttribute("customers", customerPage.getContent());
        model.addAttribute("currentPage", customerPage.getNumber() + 1);
        model.addAttribute("totalPages", customerPage.getTotalPages());
        return "customer/customer-table";
    }

    @GetMapping("/find")
    public String findCustomers(@RequestParam("name") String name, Model model) {
        var customers = customerService.findCustomers(name);
        model.addAttribute("customers", customers);
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

    @PostMapping("/delete/{id}")
    public String deleteCustomer(@PathVariable("id") long id, RedirectAttributes redirectAttributes) {
        try {
            customerService.deleteCustomer(id);
        } catch (CustomerDeletionNotAllowedException e) {
            redirectAttributes.addFlashAttribute("deleteNotAllowed", true);
        }
        return "redirect:/customers/list";
    }

}
