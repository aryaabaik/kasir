
package com.kasir.amana.Controller.Admin;

import com.kasir.amana.Service.TransactionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminTransactionController {

    private final TransactionService transactionService;

    public AdminTransactionController(
            TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("/admin/transactions")
    public String transactionHistory(Model model) {
        model.addAttribute(
                "transactions",
                transactionService.findLatestTransactions()
        );

        return "admin/transactions/index";
    }
}
