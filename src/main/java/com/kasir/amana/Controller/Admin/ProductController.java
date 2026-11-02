
package com.kasir.amana.Controller.Admin;

import com.kasir.amana.Model.Category;
import com.kasir.amana.Model.Product;
import com.kasir.amana.Model.ProductVariant;
import com.kasir.amana.Service.CategoryService;
import com.kasir.amana.Service.FileStorageService;
import com.kasir.amana.Service.ProductService;
import com.kasir.amana.Service.ProductVariantService;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Controller
@RequestMapping("/products")
@PreAuthorize("hasRole('ADMIN')")
public class ProductController {

    private final ProductService productService;
    private final ProductVariantService productVariantService;
    private final CategoryService categoryService;
    private final FileStorageService fileStorageService;

    public ProductController(
            ProductService productService,
            ProductVariantService productVariantService,
            CategoryService categoryService,
            FileStorageService fileStorageService) {
        this.productService = productService;
        this.productVariantService = productVariantService;
        this.categoryService = categoryService;
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/{id}/delete")
    public String deleteProduct(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            Product deletedProduct = productService.deleteProduct(id);
            fileStorageService.deleteProductImage(deletedProduct.getImageUrl());
            redirectAttributes.addFlashAttribute(
                    "success", "Produk berhasil dihapus.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute(
                    "error", exception.getMessage());
        } catch (DataIntegrityViolationException exception) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "Produk tidak dapat dihapus karena masih digunakan dalam riwayat transaksi, pembelian, atau mutasi stok. Nonaktifkan produk dari halaman detail.");
        }

        return "redirect:/products";
    }

    @PostMapping("/{id}/deactivate")
    public String deactivateProduct(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            productService.deactivateProduct(id);
            redirectAttributes.addFlashAttribute(
                    "success", "Produk berhasil dinonaktifkan."
            );
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute(
                    "error", e.getMessage()
            );
        }

        return "redirect:/products/" + id;
    }

    @PostMapping("/{id}/activate")
    public String activateProduct(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            productService.activateProduct(id);
            redirectAttributes.addFlashAttribute(
                    "success", "Produk berhasil diaktifkan kembali."
            );
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute(
                    "error", e.getMessage()
            );
        }

        return "redirect:/products/" + id;
    }

    @GetMapping("/{id}/variants/create")
    public String createVariant(
            @PathVariable Long id,
            Model model) {

        Product product = productService.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Produk tidak ditemukan."));

        model.addAttribute("product", product);
        return "admin/products/add-variant";
    }

    @PostMapping("/{id}/variants")
    public String storeVariant(
            @PathVariable Long id,
            @RequestParam String size,
            @RequestParam String color,
            @RequestParam BigDecimal purchasePrice,
            @RequestParam BigDecimal sellingPrice,
            @RequestParam Integer stock,
            @RequestParam(defaultValue = "5") Integer minimumStock,
            RedirectAttributes redirectAttributes) {

        try {
            ProductVariant variant = productService.addVariant(
                    id, size, color, purchasePrice, sellingPrice,
                    stock, minimumStock);

            redirectAttributes.addFlashAttribute(
                    "success", "Varian berhasil ditambahkan. SKU: "
                            + variant.getSku());

            return "redirect:/products/" + id;

        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute(
                    "error", exception.getMessage());

            return "redirect:/products/" + id + "/variants/create";
        }
    }

    @GetMapping
    public String index(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean active,
            Model model) {

        List<Product> products = productService.searchProducts(
                keyword,
                categoryId,
                active
        );

        List<ProductVariant> variants =
                productVariantService.findByProductIdIn(
                        products.stream()
                                .map(Product::getId)
                                .toList()
                );

        Map<Long, Integer> variantCountByProduct = new HashMap<>();
        Map<Long, Integer> stockByProduct = new HashMap<>();
        Map<Long, List<ProductVariant>> variantsByProduct = new HashMap<>();
        Map<Long, String> priceDisplayByProduct = new HashMap<>();
        Map<Long, BigDecimal> minPriceByProduct = new HashMap<>();

        Locale localeId = Locale.forLanguageTag("id-ID");
        NumberFormat currencyFormat = NumberFormat.getNumberInstance(localeId);

        for (ProductVariant variant : variants) {
            Long productId = variant.getProduct().getId();

            variantCountByProduct.merge(productId, 1, Integer::sum);
            stockByProduct.merge(productId, variant.getStock(), Integer::sum);

            variantsByProduct
                    .computeIfAbsent(productId, key -> new ArrayList<>())
                    .add(variant);
        }

        for (Product product : products) {
            List<ProductVariant> productVariants = variantsByProduct.getOrDefault(product.getId(), List.of());
            if (productVariants.isEmpty()) {
                priceDisplayByProduct.put(product.getId(), "-");
            } else {
                BigDecimal minPrice = productVariants.stream()
                        .map(ProductVariant::getSellingPrice)
                        .min(BigDecimal::compareTo)
                        .orElse(BigDecimal.ZERO);
                BigDecimal maxPrice = productVariants.stream()
                        .map(ProductVariant::getSellingPrice)
                        .max(BigDecimal::compareTo)
                        .orElse(BigDecimal.ZERO);

                minPriceByProduct.put(product.getId(), minPrice);

                if (minPrice.compareTo(maxPrice) == 0) {
                    priceDisplayByProduct.put(product.getId(), "Rp " + currencyFormat.format(minPrice));
                } else {
                    priceDisplayByProduct.put(product.getId(), "Rp " + currencyFormat.format(minPrice) + " - " + currencyFormat.format(maxPrice));
                }
            }
        }

        model.addAttribute("products", products);
        model.addAttribute("variantCountByProduct", variantCountByProduct);
        model.addAttribute("stockByProduct", stockByProduct);
        model.addAttribute("variantsByProduct", variantsByProduct);
        model.addAttribute("priceDisplayByProduct", priceDisplayByProduct);
        model.addAttribute("minPriceByProduct", minPriceByProduct);

        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("selectedActive", active);

        return "admin/products/index";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        Product product = productService.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Produk tidak ditemukan"));

        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute(
                "variants",
                productVariantService.findByProductId(id));

        return "admin/products/edit";
    }

    @PostMapping("/{id}/edit")
    public String update(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam Long categoryId,
            @RequestParam(required = false) String description,
            @RequestParam(value = "image", required = false) MultipartFile imageFile,
            @RequestParam(value = "removeImage", defaultValue = "false") boolean removeImage,
            @RequestParam List<Long> variantIds,
            @RequestParam List<String> sizes,
            @RequestParam List<String> colors,
            @RequestParam List<BigDecimal> purchasePrices,
            @RequestParam List<BigDecimal> sellingPrices,
            @RequestParam List<Integer> stocks,
            @RequestParam List<Integer> minimumStocks,
            RedirectAttributes redirectAttributes) {

        if (variantIds.size() != sizes.size()
                || sizes.size() != colors.size()
                || sizes.size() != purchasePrices.size()
                || sizes.size() != sellingPrices.size()
                || sizes.size() != stocks.size()
                || sizes.size() != minimumStocks.size()) {
            redirectAttributes.addFlashAttribute(
                    "error", "Data varian tidak lengkap.");
            return "redirect:/products/" + id + "/edit";
        }

        Product existingProduct = productService.findById(id)
                .orElse(null);
        if (existingProduct == null) {
            redirectAttributes.addFlashAttribute("error", "Produk tidak ditemukan.");
            return "redirect:/products";
        }

        String newUploadedImageUrl = null;
        try {
            if (imageFile != null && !imageFile.isEmpty()) {
                newUploadedImageUrl = fileStorageService.storeProductImage(imageFile);
            }
        } catch (IllegalArgumentException | SecurityException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
            return "redirect:/products/" + id + "/edit";
        }

        try {
            Category category = categoryService.findById(categoryId)
                    .orElseThrow(() ->
                            new IllegalArgumentException("Kategori tidak ditemukan"));

            List<ProductVariant> editedVariants = new ArrayList<>();

            for (int i = 0; i < variantIds.size(); i++) {
                ProductVariant variant = new ProductVariant();
                variant.setId(variantIds.get(i));
                variant.setSize(sizes.get(i));
                variant.setColor(colors.get(i));
                variant.setPurchasePrice(purchasePrices.get(i));
                variant.setSellingPrice(sellingPrices.get(i));
                variant.setStock(stocks.get(i));
                variant.setMinimumStock(minimumStocks.get(i));

                editedVariants.add(variant);
            }

            String oldImageUrl = existingProduct.getImageUrl();
            boolean shouldRemoveOldImage = (removeImage || newUploadedImageUrl != null) && oldImageUrl != null;

            productService.updateWithVariants(
                    id, name, category, description, newUploadedImageUrl, removeImage, editedVariants);

            if (shouldRemoveOldImage) {
                fileStorageService.deleteProductImage(oldImageUrl);
            }

            redirectAttributes.addFlashAttribute(
                    "success", "Produk berhasil diperbarui.");

            return "redirect:/products/" + id;

        } catch (IllegalArgumentException exception) {
            // Cleanup newly uploaded file if validation failed
            if (newUploadedImageUrl != null) {
                fileStorageService.deleteProductImage(newUploadedImageUrl);
            }
            redirectAttributes.addFlashAttribute(
                    "error", exception.getMessage());
            return "redirect:/products/" + id + "/edit";
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Product product = productService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Produk tidak ditemukan"));
        List<ProductVariant> variants = productVariantService.findByProductId(id);
        int totalStock = variants.stream().mapToInt(ProductVariant::getStock).sum();

        model.addAttribute("product", product);
        model.addAttribute("variants", variants);
        model.addAttribute("totalStock", totalStock);
        return "admin/products/detail";
    }

    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("productCodePreview", productService.previewNextCode());
        return "admin/products/create";
    }

    @PostMapping
    public String store(
            @RequestParam String name,
            @RequestParam Long categoryId,
            @RequestParam(required = false) String description,
            @RequestParam(value = "image", required = false) MultipartFile imageFile,
            @RequestParam List<String> sizes,
            @RequestParam List<String> colors,
            @RequestParam List<Integer> stocks,
            @RequestParam BigDecimal purchasePrice,
            @RequestParam BigDecimal sellingPrice,
            @RequestParam(defaultValue = "5") Integer minimumStock,
            RedirectAttributes redirectAttributes) {

        if (sizes.isEmpty() || sizes.size() != colors.size() || sizes.size() != stocks.size()) {
            redirectAttributes.addFlashAttribute("error", "Lengkapi data setiap varian produk.");
            return "redirect:/products/create";
        }

        Set<String> variantCombinations = new HashSet<>();
        for (int i = 0; i < sizes.size(); i++) {
            String size = sizes.get(i).trim();
            String color = colors.get(i).trim();
            if (size.isEmpty() || color.isEmpty() || stocks.get(i) == null || stocks.get(i) < 0) {
                redirectAttributes.addFlashAttribute("error", "Warna, ukuran, dan stok varian harus valid.");
                return "redirect:/products/create";
            }
            String combination = color.toLowerCase(Locale.ROOT)
                    + "|" + size.toLowerCase(Locale.ROOT);
            if (!variantCombinations.add(combination)) {
                redirectAttributes.addFlashAttribute(
                        "error", "Kombinasi warna dan ukuran tidak boleh duplikat.");
                return "redirect:/products/create";
            }
        }

        String imageUrl = null;
        try {
            if (imageFile != null && !imageFile.isEmpty()) {
                imageUrl = fileStorageService.storeProductImage(imageFile);
            }
        } catch (IllegalArgumentException | SecurityException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
            return "redirect:/products/create";
        }

        Category category = categoryService.findById(categoryId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Kategori tidak ditemukan"));

        Product product = new Product();
        product.setName(name);
        product.setCategory(category);
        product.setDescription(description);
        product.setImageUrl(imageUrl);
        product.setActive(true);

        List<ProductVariant> variants = new ArrayList<>();
        for (int i = 0; i < sizes.size(); i++) {
            ProductVariant variant = new ProductVariant();
            variant.setSize(sizes.get(i).trim());
            variant.setColor(colors.get(i).trim());
            variant.setPurchasePrice(purchasePrice);
            variant.setSellingPrice(sellingPrice);
            variant.setStock(stocks.get(i));
            variant.setMinimumStock(minimumStock);
            variant.setActive(true);
            variants.add(variant);
        }

        ProductService.SaveResult result;
        try {
            result = productService.saveWithVariants(product, variants);
        } catch (IllegalArgumentException exception) {
            if (imageUrl != null) {
                fileStorageService.deleteProductImage(imageUrl);
            }
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
            return "redirect:/products/create";
        }

        redirectAttributes.addFlashAttribute(
                "success", (result.merged()
                        ? "Varian berhasil digabung ke produk "
                        : "Produk berhasil ditambahkan. Kode: ")
                        + result.product().getCode() + " · "
                        + variants.size() + " varian berhasil disimpan.");

        return "redirect:/products";
    }
}
