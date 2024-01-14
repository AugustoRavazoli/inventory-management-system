package io.github.augustoravazoli.inventorymanagementsystem.customer;

import io.github.augustoravazoli.inventorymanagementsystem.user.User;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public String createCustomer(@AuthenticationPrincipal User user, @Valid @ModelAttribute CustomerForm customer, Model model) {
        try {
            customerService.createCustomer(customer.toEntity(), user);
        } catch (CustomerNameTakenException e) {
            model.addAttribute("duplicatedName", true);
            model.addAttribute("customer", customer);
            model.addAttribute("mode", "create");
            return "customer/customer-form";
        }
        return "redirect:/customers/list";
    }

    @GetMapping("/list")
    public String listCustomers(@AuthenticationPrincipal User user, @RequestParam(name = "page", defaultValue = "1") int page, Model model) {
        var customerPage = customerService.listCustomers(page, user);
        model.addAttribute("customers", customerPage.getContent());
        model.addAttribute("currentPage", customerPage.getNumber() + 1);
        model.addAttribute("totalPages", customerPage.getTotalPages());
        return "customer/customer-table";
    }

    @GetMapping("/find")
    public String findCustomers(@AuthenticationPrincipal User user, @RequestParam("name") String name, Model model) {
        var customers = customerService.findCustomers(name, user);
        model.addAttribute("customers", customers);
        return "customer/customer-table";
    }

    @GetMapping("/update/{id}")
    public String retrieveUpdateCustomerPage(@AuthenticationPrincipal User user, @PathVariable("id") long id, Model model) {
        var customer = customerService.findCustomer(id, user);
        model.addAttribute("customer", customer.toForm());
        model.addAttribute("id", customer.getId());
        model.addAttribute("mode", "update");
        return "customer/customer-form";
    }

    @PostMapping("/update/{id}")
    public String updateCustomer(@AuthenticationPrincipal User user, @PathVariable("id") long id, @Valid @ModelAttribute CustomerForm customer, Model model) {
        try {
            customerService.updateCustomer(id, customer.toEntity(), user);
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
    public String deleteCustomer(@AuthenticationPrincipal User user, @PathVariable("id") long id, RedirectAttributes redirectAttributes) {
        try {
            customerService.deleteCustomer(id, user);
        } catch (CustomerDeletionNotAllowedException e) {
            redirectAttributes.addFlashAttribute("deleteNotAllowed", true);
        }
        return "redirect:/customers/list";
    }

}
