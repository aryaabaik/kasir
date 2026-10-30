
package com.kasir.amana.Controller.Admin;

import com.kasir.amana.Service.UserService;
import com.kasir.amana.Service.UserService.CashierDeletionResult;
import com.kasir.amana.Service.UserService.CashierStatusResult;
import com.kasir.amana.Model.User;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.Optional;

@Controller
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/admin/users")
    public String index(Model model) {
        model.addAttribute("users", userService.findAll());
        return "admin/users/index";
    }

    @PostMapping("/admin/users/{id}/delete")
    public String deleteCashier(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        CashierDeletionResult result = userService.deleteCashier(id);

        switch (result) {
            case DELETED -> redirectAttributes.addFlashAttribute(
                    "successMessage", "Akun kasir berhasil dihapus.");
            case NOT_FOUND -> redirectAttributes.addFlashAttribute(
                    "errorMessage", "Pengguna tidak ditemukan.");
            case NOT_CASHIER -> redirectAttributes.addFlashAttribute(
                    "errorMessage", "Akun admin tidak dapat dihapus.");
            case HAS_TRANSACTIONS -> redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Kasir memiliki riwayat transaksi dan tidak dapat dihapus.");
        }

        return "redirect:/admin/users";
    }

    @PostMapping("/admin/users/{id}/toggle-status")
    public String toggleCashierStatus(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        CashierStatusResult result = userService.toggleCashierStatus(id);

        switch (result) {
            case ACTIVATED -> redirectAttributes.addFlashAttribute(
                    "successMessage", "Akun kasir berhasil diaktifkan.");
            case DEACTIVATED -> redirectAttributes.addFlashAttribute(
                    "successMessage", "Akun kasir berhasil dinonaktifkan.");
            case NOT_FOUND -> redirectAttributes.addFlashAttribute(
                    "errorMessage", "Pengguna tidak ditemukan.");
            case NOT_CASHIER -> redirectAttributes.addFlashAttribute(
                    "errorMessage", "Status akun admin tidak dapat diubah.");
        }

        return "redirect:/admin/users";
    }

    @GetMapping("/admin/users/new")
    public String createForm() {
        return "admin/users/create";
    }

    @PostMapping("/admin/users")
    public String store(
            @RequestParam String username,
            @RequestParam String fullName,
            @RequestParam String password,
            RedirectAttributes redirectAttributes) {

        try {
            userService.createCashier(username, fullName, password);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Akun Kasir berhasil dibuat.");

            return "redirect:/admin/users";

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", e.getMessage());

            return "redirect:/admin/users/new";
        }
    }

    @GetMapping("/admin/users/{id}/edit")
     public String editForm(@PathVariable Long id, Model model,
                        RedirectAttributes redirectAttributes) {
        Optional<User> user = userService.findById(id);

        if (user.isEmpty()) {
            redirectAttributes.addFlashAttribute(
                    "error", "Pengguna tidak ditemukan.");
            return "redirect:/admin/users";
        }

        model.addAttribute("user", user.get());
        return "admin/users/edit";
    }

    @PostMapping("/admin/users/{id}/edit")
    public String update(@PathVariable Long id,
                        @RequestParam String username,
                        @RequestParam String fullName,
                        RedirectAttributes redirectAttributes) {
        try {
            userService.updateUser(id, username, fullName);
            redirectAttributes.addFlashAttribute(
                    "success", "Data pengguna berhasil diperbarui.");
            return "redirect:/admin/users";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/users/" + id + "/edit";
        }
    }

    @PostMapping("/admin/users/{id}/toggle-active")
    public String toggleActive(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            User user = userService.toggleActive(id);

            String status = Boolean.TRUE.equals(user.getActive())
                    ? "diaktifkan"
                    : "dinonaktifkan";

            redirectAttributes.addFlashAttribute(
                    "successMessage", "Akun berhasil " + status + ".");

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/users";
    }
}
