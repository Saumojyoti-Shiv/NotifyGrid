package com.bulksms.billing_service.controller;

import com.bulksms.billing_service.model.BillingAccount;
import com.bulksms.billing_service.model.BillingHistory;
import com.bulksms.billing_service.repository.BillingAccountRepository;
import com.bulksms.billing_service.repository.BillingHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/billing")
public class BillingController {

    @Autowired
    private BillingAccountRepository accountRepository;

    @Autowired
    private com.bulksms.billing_service.repository.BillingHistoryRepository historyRepository;

    @PostMapping("/recharge")
    public void recharge(@RequestBody com.bulksms.billing_service.dto.RechargeRequest request) {
        BillingAccount account = accountRepository.findByUsername(request.getUsername())
                .orElseGet(() -> {
                    BillingAccount newAcc = new BillingAccount();
                    newAcc.setUsername(request.getUsername());
                    newAcc.setCredits(0);
                    return accountRepository.save(newAcc);
                });
        
        account.setCredits(account.getCredits() + request.getAmount());
        accountRepository.save(account);

        BillingHistory history = new BillingHistory();
        history.setUsername(request.getUsername());
        history.setAmount(request.getAmount());
        history.setType("RECHARGE");
        history.setDescription("Manual credit restoration");
        historyRepository.save(history);
    }

    @GetMapping("/balance/{username}")
    public Integer getBalance(@PathVariable String username) {
        return accountRepository.findByUsername(username)
                .map(BillingAccount::getCredits)
                .orElse(0);
    }

    @GetMapping("/history/{username}")
    public List<BillingHistory> getHistory(@PathVariable String username) {
        return historyRepository.findByUsername(username);
    }
}
