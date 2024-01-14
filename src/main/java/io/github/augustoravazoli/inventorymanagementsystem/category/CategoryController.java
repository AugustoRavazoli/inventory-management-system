package io.github.augustoravazoli.inventorymanagementsystem.category;

import io.github.augustoravazoli.inventorymanagementsystem.user.User;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public String createCategory(@AuthenticationPrincipal User user, @Valid @ModelAttribute("category") CategoryForm category, Model model) {
        try {
            categoryService.createCategory(category.toEntity(), user);
        } catch (CategoryNameTakenException e) {
            model.addAttribute("duplicatedName", true);
            model.addAttribute("category", category);
            model.addAttribute("mode", "create");
            return "category/category-form";
        }
        return "redirect:/categories/list";
    }

    @GetMapping("/list")
    public String listCategories(@AuthenticationPrincipal User user, @RequestParam(name = "page", defaultValue = "1") int page, Model model) {
        var categoryPage = categoryService.listCategories(page, user);
        model.addAttribute("categories", categoryPage.getContent());
        model.addAttribute("currentPage", categoryPage.getNumber() + 1);
        model.addAttribute("totalPages", categoryPage.getTotalPages());
        return "category/category-table";
    }

    @GetMapping("/find")
    public String findCategories(@AuthenticationPrincipal User user, @RequestParam("name") String name, Model model) {
        var categories = categoryService.findCategories(name, user);
        model.addAttribute("categories", categories);
        return "category/category-table";
    }

    @GetMapping("/update/{id}")
    public String retrieveUpdateCategoryPage(@AuthenticationPrincipal User user, @PathVariable("id") long id, Model model) {
        var category = categoryService.findCategory(id, user);
        model.addAttribute("category", category.toForm());
        model.addAttribute("id", category.getId());
        model.addAttribute("mode", "update");
        return "category/category-form";
    }

    @PostMapping("/update/{id}")
    public String updateCategory(@AuthenticationPrincipal User user, @PathVariable("id") long id, @Valid @ModelAttribute CategoryForm category, Model model) {
        try {
            categoryService.updateCategory(id, category.toEntity(), user);
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
    public String deleteCategory(@AuthenticationPrincipal User user, @PathVariable("id") long id) {
        categoryService.deleteCategory(id, user);
        return "redirect:/categories/list";
    }

}
