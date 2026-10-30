
package com.kasir.amana.Controller.Admin;

import com.kasir.amana.Model.ProductVariant;
import com.kasir.amana.Model.StockMovement;
import com.kasir.amana.Model.User;
import com.kasir.amana.Repository.UserRepository;
import com.kasir.amana.Service.ProductVariantService;
import com.kasir.amana.Service.StockMovementService;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/stock")
public class StockMovementController {

    private final StockMovementService stockMovementService;
    private final ProductVariantService productVariantService;
    private final UserRepository userRepository;

    public StockMovementController(
            StockMovementService stockMovementService,
            ProductVariantService productVariantService,
            UserRepository userRepository) {
        this.stockMovementService = stockMovementService;
        this.productVariantService = productVariantService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String index(Model model) {
        List<ProductVariant> variants =
                productVariantService.findAll();
        List<ProductVariant> activeVariants = variants.stream()
                .filter(variant -> Boolean.TRUE.equals(variant.getActive()))
                .filter(variant -> variant.getProduct() != null
                        && Boolean.TRUE.equals(variant.getProduct().getActive()))
                .toList();

        List<StockMovement> movements =
                stockMovementService.findAllLatest();

        model.addAttribute("variants", variants);
        model.addAttribute("activeVariants", activeVariants);
        model.addAttribute("movements", movements);

        return "admin/stock/index";
    }

    @PostMapping("/in")
    public String stockIn(
            @RequestParam Long variantId,
            @RequestParam Integer quantity,
            @RequestParam(required = false) String notes,
            Authentication authentication) {

        User user = getLoggedInUser(authentication);

        stockMovementService.adjustStock(
                variantId, user, "IN", quantity, notes);

        return "redirect:/admin/stock";
    }

    @PostMapping("/out")
    public String stockOut(
            @RequestParam Long variantId,
            @RequestParam Integer quantity,
            @RequestParam(required = false) String notes,
            Authentication authentication) {

        User user = getLoggedInUser(authentication);

        stockMovementService.adjustStock(
                variantId, user, "OUT", quantity, notes);

        return "redirect:/admin/stock";
    }

    private User getLoggedInUser(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Pengguna yang login tidak ditemukan."));
    }
}
