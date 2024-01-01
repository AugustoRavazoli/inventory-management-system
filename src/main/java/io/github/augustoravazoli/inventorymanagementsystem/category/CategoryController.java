package io.github.augustoravazoli.inventorymanagementsystem.category;

import jakarta.validation.Valid;
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
        model.addAttribute("mode", "create");
        return "category/category-form";
    }

    @PostMapping("/create")
    public String createCategory(@Valid @ModelAttribute CategoryForm category, Model model) {
        try {
            categoryService.createCategory(category.toEntity());
        } catch (CategoryNameTakenException e) {
            model.addAttribute("duplicatedName", true);
            model.addAttribute("category", category);
            model.addAttribute("mode", "create");
            return "category/category-form";
        }
        return "redirect:/categories/list";
    }

    @GetMapping("/list")
    public String listCategories(@RequestParam(name = "page", defaultValue = "1") int page, Model model) {
        var categoryPage = categoryService.listCategories(page);
        model.addAttribute("categories", categoryPage.getContent());
        model.addAttribute("currentPage", categoryPage.getNumber() + 1);
        model.addAttribute("totalPages", categoryPage.getTotalPages());
        return "category/category-table";
    }

    @GetMapping("/find")
    public String findCategories(@RequestParam("name") String name, Model model) {
        var categories = categoryService.findCategories(name);
        model.addAttribute("categories", categories);
        return "category/category-table";
    }

    @GetMapping("/update/{id}")
    public String retrieveUpdateCategoryPage(@PathVariable("id") long id, Model model) {
        var category = categoryService.findCategory(id);
        model.addAttribute("category", category.toForm());
        model.addAttribute("id", category.getId());
        model.addAttribute("mode", "update");
        return "category/category-form";
    }

    @PostMapping("/update/{id}")
    public String updateCategory(@PathVariable("id") long id, @Valid @ModelAttribute CategoryForm category, Model model) {
        try {
            categoryService.updateCategory(id, category.toEntity());
        } catch (CategoryNameTakenException e) {
            model.addAttribute("duplicatedName", true);
            model.addAttribute("category", category);
            model.addAttribute("id", id);
            model.addAttribute("mode", "update");
            return "category/category-form";
        }
        return "redirect:/categories/list";
    }

    @PostMapping("/delete/{id}")
    public String deleteCategory(@PathVariable("id") long id) {
        categoryService.deleteCategory(id);
        return "redirect:/categories/list";
    }

}
