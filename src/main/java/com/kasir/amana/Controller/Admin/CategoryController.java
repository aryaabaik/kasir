package com.kasir.amana.Controller.Admin;

import com.kasir.amana.Model.Category;
import com.kasir.amana.Service.CategoryService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/categories")
@PreAuthorize("hasRole('ADMIN')")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    //Menampilkan daftar kategori
    @GetMapping
    public String index(Model model) {
        model.addAttribute(
            "categories",
            categoryService.findAll()
        );

        return "admin/categories/index";
    }

    //Menampilkan form tambah kategori
    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("category", new Category());
        return "admin/categories/create";
    }

    //Menyimpan kategori baru
    @PostMapping
    public String store(
            @ModelAttribute Category category,
            RedirectAttributes redirectAttributes) {

        categoryService.save(category);
        redirectAttributes.addFlashAttribute(
            "success",
            "Kategori berhasil ditambahkan."
        );

        return "redirect:/categories";
    }

    //Menghapus kategori
    @PostMapping("/delete/{id}")
    public String delete(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        if (categoryService.findById(id).isPresent()) {
            categoryService.deleteById(id);
            redirectAttributes.addFlashAttribute(
                "success",
                "Kategori berhasil dihapus."
            );
        } else {
            redirectAttributes.addFlashAttribute(
                "error",
                "Kategori tidak ditemukan."
            );
        }

        return "redirect:/categories";
    }
}
