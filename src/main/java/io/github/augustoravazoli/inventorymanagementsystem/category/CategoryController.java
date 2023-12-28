package io.github.augustoravazoli.inventorymanagementsystem.category;

import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/create")
    public String retrieveCreateCategoryPage(Model model) {
        model.addAttribute("category", new CategoryForm());
        return "category/category-form";
    }

    @PostMapping("/create")
    public String createCategory(@Valid @ModelAttribute CategoryForm category, Model model) {
        try {
            categoryService.createCategory(category.toEntity());
        } catch (CategoryNameTakenException e) {
            model.addAttribute("duplicatedName", true);
            model.addAttribute("category", category);
            return "category/category-form";
        }
        return "redirect:/categories/list";
    }


    @GetMapping("/list")
    public String listCategories(@RequestParam(name = "name", defaultValue = "") String name, Pageable pageable, Model model) {
        var categoryPage = categoryService.listCategories(name,pageable);
        model.addAttribute("categories", categoryPage.getContent());
        model.addAttribute("currentPage", categoryPage.getNumber() + 1);
        model.addAttribute("totalPages", categoryPage.getTotalPages());
        return "category/category-table";
    }

}
