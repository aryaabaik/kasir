
package com.kasir.amana.Controller.Kasir;

import com.kasir.amana.Model.Product;
import com.kasir.amana.Model.ProductVariant;
import com.kasir.amana.Service.CheckoutService;
import com.kasir.amana.Service.ProductService;
import com.kasir.amana.Service.ProductVariantService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import com.kasir.amana.Model.Transaction;

@Controller
public class KasirTransactionController {

    private final CheckoutService checkoutService;
    private final ProductService productService;
    private final ProductVariantService variantService;

    public KasirTransactionController(
            CheckoutService checkoutService,
            ProductService productService,
            ProductVariantService variantService) {
        this.checkoutService = checkoutService;
        this.productService = productService;
        this.variantService = variantService;
    }

    @GetMapping("/kasir/transactions")
    public String checkoutPage(Model model) {
        // Load active products that have at least one active variant
        List<ProductVariant> activeVariants = variantService.findActiveVariants()
                .stream()
                .filter(v -> Boolean.TRUE.equals(v.getProduct().getActive()))
                .toList();

        // Group by product to get distinct products
        Map<Long, List<ProductVariant>> variantsByProduct = activeVariants.stream()
                .collect(Collectors.groupingBy(v -> v.getProduct().getId()));

        // Create product summaries for display
        List<ProductCatalogItem> catalogItems = variantsByProduct.entrySet().stream()
                .map(entry -> {
                    List<ProductVariant> variants = entry.getValue();
                    ProductVariant firstVariant = variants.get(0);
                    Product product = firstVariant.getProduct();

                    // Calculate aggregates
                    int variantCount = variants.size();
                    int totalStock = variants.stream()
                            .mapToInt(ProductVariant::getStock)
                            .sum();
                    BigDecimal minPrice = variants.stream()
                            .map(ProductVariant::getSellingPrice)
                            .min(BigDecimal::compareTo)
                            .orElse(BigDecimal.ZERO);
                    BigDecimal maxPrice = variants.stream()
                            .map(ProductVariant::getSellingPrice)
                            .max(BigDecimal::compareTo)
                            .orElse(BigDecimal.ZERO);

                    return new ProductCatalogItem(
                            product.getId(),
                            product.getCode(),
                            product.getName(),
                            product.getCategory().getName(),
                            product.getImageUrl(),
                            variantCount,
                            totalStock,
                            minPrice,
                            maxPrice
                    );
                })
                .sorted(Comparator.comparing(ProductCatalogItem::name))
                .toList();

        model.addAttribute("products", catalogItems);
        model.addAttribute("checkoutUrl", "/kasir/transactions/checkout");
        model.addAttribute("dashboardUrl", "/kasir/dashboard");

        return "kasir/transactions/checkout";
    }

    @GetMapping("/kasir/transactions/variants/{productId}")
    @ResponseBody
    public ResponseEntity<?> getProductVariants(@PathVariable Long productId) {
        Optional<Product> productOpt = productService.findById(productId);
        
        if (productOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Product product = productOpt.get();
        
        if (!Boolean.TRUE.equals(product.getActive())) {
            return ResponseEntity.badRequest().body("Produk tidak aktif");
        }

        List<ProductVariant> variants = variantService.findByProductId(productId)
                .stream()
                .filter(v -> Boolean.TRUE.equals(v.getActive()))
                .sorted(Comparator.comparing(ProductVariant::getColor)
                        .thenComparing(ProductVariant::getSize))
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("productId", product.getId());
        response.put("productName", product.getName());
        response.put("productCode", product.getCode());
        response.put("imageUrl", product.getImageUrl());
        response.put("variants", variants.stream()
                .map(v -> Map.of(
                        "id", v.getId(),
                        "sku", v.getSku(),
                        "color", v.getColor(),
                        "size", v.getSize(),
                        "sellingPrice", v.getSellingPrice(),
                        "stock", v.getStock()
                ))
                .toList());

        return ResponseEntity.ok(response);
    }

    // DTO for catalog display
    public record ProductCatalogItem(
            Long id,
            String code,
            String name,
            String categoryName,
            String imageUrl,
            int variantCount,
            int totalStock,
            BigDecimal minPrice,
            BigDecimal maxPrice
    ) {}

    @PostMapping("/kasir/transactions/checkout")
    public String checkout(
            Authentication authentication,
            @RequestParam("variantId") List<Long> variantIds,
            @RequestParam("quantity") List<Integer> quantities,
            @RequestParam("amountReceived") BigDecimal amountReceived,
            RedirectAttributes redirectAttributes) {

        try {
            if (variantIds.size() != quantities.size()) {
                throw new IllegalArgumentException(
                        "Data keranjang tidak valid.");
            }

            List<CheckoutService.CheckoutItem> items =
                    IntStream.range(0, variantIds.size())
                            .filter(i -> quantities.get(i) != null
                                    && quantities.get(i) > 0)
                            .mapToObj(i ->
                                    new CheckoutService.CheckoutItem(
                                            variantIds.get(i),
                                            quantities.get(i)))
                            .toList();

            var transaction = checkoutService.checkout(
                    authentication.getName(),
                    items,
                    amountReceived
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Transaksi berhasil. Nomor invoice: "
                            + transaction.getInvoiceNumber());

            return "redirect:/kasir/transactions/receipt/"
             + transaction.getInvoiceNumber();

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", e.getMessage());
        }

        return "redirect:/kasir/transactions";
    }

    @GetMapping("/kasir/transactions/history")
    public String transactionHistory(
            Authentication authentication,
            Model model) {

        model.addAttribute(
                "transactions",
                checkoutService.findCashierTransactions(
                        authentication.getName()));

        model.addAttribute("dashboardUrl", "/kasir/dashboard");

        return "kasir/transactions/history";
    }

    @GetMapping("/kasir/transactions/receipt/{invoiceNumber}")
    public String receipt(
            @PathVariable String invoiceNumber,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {

        var result = checkoutService.findReceiptTransaction(invoiceNumber);

        if (result.isEmpty()) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "Transaksi tidak ditemukan.");
            return "redirect:/kasir/transactions";
        }

        Transaction transaction = result.get();

        // Kasir hanya boleh melihat struk miliknya sendiri.
        if (!transaction.getCashier().getUsername()
                .equals(authentication.getName())) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "Kamu tidak memiliki akses ke struk ini.");
            return "redirect:/kasir/transactions";
        }

        model.addAttribute("transaction", transaction);
        model.addAttribute(
                "items",
                checkoutService.findReceiptItems(transaction.getId()));

        return "kasir/transactions/receipt";
    }
}
