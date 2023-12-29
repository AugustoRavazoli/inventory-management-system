package io.github.augustoravazoli.inventorymanagementsystem.product;

import io.github.augustoravazoli.inventorymanagementsystem.category.CategoryService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
    public String retrieveCreateProductPage(Model model) {
        model.addAttribute("product", new ProductForm());
        model.addAttribute("categories", categoryService.listCategories());
        model.addAttribute("mode", "create");
        return "product/product-form";
    }

    @PostMapping("/create")
    public String createProduct(@Valid @ModelAttribute ProductForm product, Model model) {
        try {
            productService.createProduct(product.toEntity());
        } catch (ProductNameTakenException e) {
            model.addAttribute("duplicatedName", true);
            model.addAttribute("product", product);
            model.addAttribute("categories", categoryService.listCategories());
            model.addAttribute("mode", "create");
            return "product/product-form";
        }
        return "redirect:/products/list";
    }

    @GetMapping("/list")
    public String listProducts(@RequestParam(name = "name", defaultValue = "") String name, Pageable pageable, Model model) {
        var productPage = productService.listProducts(name, pageable);
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", productPage.getNumber() + 1);
        model.addAttribute("totalPages", productPage.getTotalPages());
        return "product/product-table";
    }

    @GetMapping("/update/{id}")
    public String retrieveUpdateProductPage(@PathVariable("id") long id, Model model) {
        var product = productService.findProduct(id);
        model.addAttribute("product", product.toForm());
        model.addAttribute("categories", categoryService.listCategories());
        model.addAttribute("id", product.getId());
        model.addAttribute("mode", "update");
        return "product/product-form";
    }

    @PostMapping("/update/{id}")
    public String updateProduct(@PathVariable("id") long id, @Valid @ModelAttribute ProductForm product, Model model) {
        try {
            productService.updateProduct(id, product.toEntity());
        } catch (ProductNameTakenException e) {
            model.addAttribute("duplicatedName", true);
            model.addAttribute("product", product);
            model.addAttribute("categories", categoryService.listCategories());
            model.addAttribute("id", id);
            model.addAttribute("mode", "update");
            return "product/product-form";
        }
        return "redirect:/products/list";
    }

    @PostMapping("/delete/{id}")
    public String deleteProduct(@PathVariable("id") long id) {
        productService.deleteProduct(id);
        return "redirect:/products/list";
    }

}
