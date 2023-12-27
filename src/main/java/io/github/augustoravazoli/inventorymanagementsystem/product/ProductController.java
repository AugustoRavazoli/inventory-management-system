package io.github.augustoravazoli.inventorymanagementsystem.product;

import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/create")
    public String retrieveCreateProductPage(Model model) {
        model.addAttribute("product", new ProductForm());
        return "product/product-form";
    }

    @PostMapping("/create")
    public String createProduct(@Valid @ModelAttribute ProductForm product, Model model) {
        try {
            productService.createProduct(product.toEntity());
        } catch (ProductNameTakenException e) {
            model.addAttribute("duplicatedName", true);
            model.addAttribute("product", product);
            return "product/product-form";
        }
        return "redirect:/products/list";
    }

    @GetMapping("/list")
    public String listProducts(Pageable pageable, Model model) {
        var productPage = productService.listProducts(pageable);
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", productPage.getNumber() + 1);
        model.addAttribute("totalPages", productPage.getTotalPages());
        return "product/product-table";
    }

}
