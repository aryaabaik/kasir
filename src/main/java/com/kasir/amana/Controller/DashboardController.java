
package com.kasir.amana.Controller;

import com.kasir.amana.Model.ProductVariant;
import com.kasir.amana.Model.Transaction;
import com.kasir.amana.Service.CategoryService;
import com.kasir.amana.Service.ProductService;
import com.kasir.amana.Service.ProductVariantService;
import com.kasir.amana.Service.TransactionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Controller
public class DashboardController {

    private final TransactionService transactionService;
    private final ProductService productService;
    private final ProductVariantService productVariantService;
    private final CategoryService categoryService;

    public DashboardController(
            TransactionService transactionService,
            ProductService productService,
            ProductVariantService productVariantService,
            CategoryService categoryService) {
        this.transactionService = transactionService;
        this.productService = productService;
        this.productVariantService = productVariantService;
        this.categoryService = categoryService;
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model, Principal principal) {
        model.addAttribute("pageTitle", "Dashboard Admin");

        // Authenticated username
        String username = (principal != null) ? principal.getName() : "Admin";
        model.addAttribute("username", username);

        // --- Today's boundaries (consistent with ReportController) ---
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.plusDays(1).atStartOfDay()
                .minusNanos(1);

        BigDecimal todaySales = transactionService
                .sumSubtotalBetween(todayStart, todayEnd);
        long todayTransactions = transactionService
                .countTransactionsBetween(todayStart, todayEnd);

        model.addAttribute("todaySales",
                todaySales != null ? todaySales : BigDecimal.ZERO);
        model.addAttribute("todayTransactions", todayTransactions);

        // --- This month's boundaries ---
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDateTime monthStartDt = monthStart.atStartOfDay();
        LocalDateTime monthEndDt = today.plusDays(1).atStartOfDay()
                .minusNanos(1);

        BigDecimal monthlySales = transactionService
                .sumSubtotalBetween(monthStartDt, monthEndDt);
        long monthlyTransactions = transactionService
                .countTransactionsBetween(monthStartDt, monthEndDt);

        model.addAttribute("monthlySales",
                monthlySales != null ? monthlySales : BigDecimal.ZERO);
        model.addAttribute("monthlyTransactions", monthlyTransactions);

        // --- Product counts ---
        long activeProductCount = productService.findAll().stream()
                .filter(p -> Boolean.TRUE.equals(p.getActive()))
                .count();
        model.addAttribute("activeProductCount", activeProductCount);

        long categoryCount = categoryService.findAll().size();
        model.addAttribute("categoryCount", categoryCount);

        // --- Low-stock variants (stock <= minimumStock) ---
        List<ProductVariant> allVariants = productVariantService.findActiveVariants();
        List<ProductVariant> lowStockVariants = allVariants.stream()
                .filter(v -> v.getStock() != null
                        && v.getMinimumStock() != null
                        && v.getStock() <= v.getMinimumStock())
                .toList();

        model.addAttribute("lowStockCount", lowStockVariants.size());
        model.addAttribute("lowStockVariants", lowStockVariants);

        // --- Recent transactions (5 most recent) ---
        model.addAttribute("recentTransactions",
                transactionService.findRecentTransactionDtos(5));

        // --- Real Daily Sales Chart (Current Month) ---
        YearMonth currentYearMonth = YearMonth.from(today);
        List<TransactionService.DailySalesDto> dailySalesList =
                transactionService.findDailySalesForMonth(currentYearMonth);
        model.addAttribute("dailySalesList", dailySalesList);

        BigDecimal maxDailySales = dailySalesList.stream()
                .map(TransactionService.DailySalesDto::getTotalSales)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        model.addAttribute("maxDailySales", maxDailySales);

        TransactionService.DailySalesDto highestSalesDay = dailySalesList.stream()
                .filter(d -> d.getTotalSales().compareTo(BigDecimal.ZERO) > 0)
                .max((a, b) -> a.getTotalSales().compareTo(b.getTotalSales()))
                .orElse(null);
        model.addAttribute("highestSalesDay", highestSalesDay);

        long activeSalesDaysCount = dailySalesList.stream()
                .filter(d -> d.getTotalSales().compareTo(BigDecimal.ZERO) > 0)
                .count();
        model.addAttribute("activeSalesDaysCount", activeSalesDaysCount);

        // --- Real Operational Summary Metrics ---
        long totalActiveVariants = allVariants.size();
        model.addAttribute("totalActiveVariants", totalActiveVariants);

        long totalStockUnits = allVariants.stream()
                .mapToLong(v -> v.getStock() != null ? v.getStock() : 0)
                .sum();
        model.addAttribute("totalStockUnits", totalStockUnits);

        long outOfStockCount = allVariants.stream()
                .filter(v -> v.getStock() != null && v.getStock() == 0)
                .count();
        model.addAttribute("outOfStockCount", outOfStockCount);

        long warningStockCount = allVariants.stream()
                .filter(v -> v.getStock() != null && v.getMinimumStock() != null
                        && v.getStock() > 0 && v.getStock() <= v.getMinimumStock())
                .count();
        model.addAttribute("warningStockCount", warningStockCount);

        long safeStockCount = allVariants.stream()
                .filter(v -> v.getStock() != null && v.getMinimumStock() != null
                        && v.getStock() > v.getMinimumStock())
                .count();
        model.addAttribute("safeStockCount", safeStockCount);

        BigDecimal averageOrderValue = BigDecimal.ZERO;
        if (monthlyTransactions > 0 && monthlySales != null) {
            averageOrderValue = monthlySales.divide(
                    BigDecimal.valueOf(monthlyTransactions), 0, RoundingMode.HALF_UP);
        }
        model.addAttribute("averageOrderValue", averageOrderValue);

        return "admin/dashboard";
    }

    @GetMapping("/kasir/dashboard")
    public String kasirDashboard(Model model, Principal principal) {
        model.addAttribute("pageTitle", "Dashboard Kasir");
        
        // Get authenticated cashier username
        String username = (principal != null) ? principal.getName() : "Kasir";
        model.addAttribute("cashierUsername", username);
        
        // Today's boundaries for cashier-specific stats
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.plusDays(1).atStartOfDay().minusNanos(1);
        
        // Fetch cashier-specific today's sales and transaction count
        BigDecimal todaySales = transactionService.sumCashierSalesBetween(username, todayStart, todayEnd);
        long todayTransactionCount = transactionService.countCashierTransactionsBetween(username, todayStart, todayEnd);
        
        model.addAttribute("todaySales", todaySales != null ? todaySales : BigDecimal.ZERO);
        model.addAttribute("todayTransactionCount", todayTransactionCount);
        
        // Fetch recent cashier transactions (last 8)
        List<Transaction> recentTransactions = transactionService.findRecentCashierTransactions(username, 8);
        model.addAttribute("recentTransactions", recentTransactions);
        
        return "kasir/dashboard";
    }
}
