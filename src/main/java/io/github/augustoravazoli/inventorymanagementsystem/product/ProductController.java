package io.github.augustoravazoli.inventorymanagementsystem.product;

import io.github.augustoravazoli.inventorymanagementsystem.category.CategoryService;
import io.github.augustoravazoli.inventorymanagementsystem.user.User;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;

    public ProductController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping("/create")
    public String retrieveCreateProductPage(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("product", new ProductForm());
        model.addAttribute("categories", categoryService.listCategories(user));
        model.addAttribute("mode", "create");
        return "product/product-form";
    }

    @PostMapping("/create")
    public String createProduct(@AuthenticationPrincipal User user, @Valid @ModelAttribute ProductForm product, Model model) {
        try {
            productService.createProduct(product.toEntity(), user);
        } catch (ProductNameTakenException e) {
            model.addAttribute("duplicatedName", true);
            model.addAttribute("product", product);
            model.addAttribute("categories", categoryService.listCategories(user));
            model.addAttribute("mode", "create");
            return "product/product-form";
        }
        return "redirect:/products/list";
    }

    @GetMapping("/list")
    public String listProducts(@AuthenticationPrincipal User user, @RequestParam(name = "page", defaultValue = "1") int page, Model model) {
        var productPage = productService.listProducts(page, user);
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", productPage.getNumber() + 1);
        model.addAttribute("totalPages", productPage.getTotalPages());
        return "product/product-table";
    }

    @GetMapping("/find")
    public String findProducts(@AuthenticationPrincipal User user, @RequestParam("name") String name, Model model) {
        var products = productService.findProducts(name, user);
        model.addAttribute("products", products);
        return "product/product-table";
    }

    @GetMapping("/update/{id}")
    public String retrieveUpdateProductPage(@AuthenticationPrincipal User user, @PathVariable("id") long id, Model model) {
        var product = productService.findProduct(id, user);
        model.addAttribute("product", product.toForm());
        model.addAttribute("categories", categoryService.listCategories(user));
        model.addAttribute("id", product.getId());
        model.addAttribute("mode", "update");
        return "product/product-form";
    }

    @PostMapping("/update/{id}")
    public String updateProduct(@AuthenticationPrincipal User user, @PathVariable("id") long id, @Valid @ModelAttribute ProductForm product, Model model) {
        try {
            productService.updateProduct(id, product.toEntity(), user);
        } catch (ProductNameTakenException e) {
            model.addAttribute("duplicatedName", true);
            model.addAttribute("product", product);
            model.addAttribute("categories", categoryService.listCategories(user));
            model.addAttribute("id", id);
            model.addAttribute("mode", "update");
            return "product/product-form";
        }
        return "redirect:/products/list";
    }

    @PostMapping("/delete/{id}")
    public String deleteProduct(@AuthenticationPrincipal User user, @PathVariable("id") long id, RedirectAttributes redirectAttributes) {
        try {
            productService.deleteProduct(id, user);
        } catch (ProductDeletionNotAllowedException e) {
            redirectAttributes.addFlashAttribute("deleteNotAllowed", true);
        }
        return "redirect:/products/list";
    }

}
