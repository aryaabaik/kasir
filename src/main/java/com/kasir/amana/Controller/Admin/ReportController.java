
package com.kasir.amana.Controller.Admin;

import com.kasir.amana.Service.TransactionService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Controller
public class ReportController {

    private final TransactionService transactionService;

    public ReportController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("/admin/reports")
    public String reports(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,
            Model model) {

        if (startDate == null) {
            startDate = LocalDate.now().withDayOfMonth(1);
        }

        if (endDate == null) {
            endDate = LocalDate.now();
        }

        if (endDate.isBefore(startDate)) {
            model.addAttribute(
                    "errorMessage",
                    "Tanggal akhir tidak boleh sebelum tanggal mulai."
            );
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);
            model.addAttribute("totalSales", BigDecimal.ZERO);
            model.addAttribute("transactionCount", 0L);
            model.addAttribute("transactions",
                    java.util.List.of());

            return "admin/reports/index";
        }

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay()
                .minusNanos(1);

        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        model.addAttribute(
                "transactions",
                transactionService.findTransactionsBetween(start, end)
        );

        model.addAttribute(
                "transactionCount",
                transactionService.countTransactionsBetween(start, end)
        );

        model.addAttribute(
                "totalSales",
                transactionService.sumSubtotalBetween(start, end)
        );

        return "admin/reports/index";
    }
}
