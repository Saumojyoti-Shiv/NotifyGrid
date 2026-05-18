package com.bulksms.billing_service.service;

import com.bulksms.billing_service.dto.CampaignSentEvent;
import com.bulksms.billing_service.model.BillingAccount;
import com.bulksms.billing_service.model.BillingHistory;
import com.bulksms.billing_service.repository.BillingAccountRepository;
import com.bulksms.billing_service.repository.BillingHistoryRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BillingService {

    @Autowired
    private BillingAccountRepository accountRepository;

    @Autowired
    private BillingHistoryRepository historyRepository;

    @Autowired
    private org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = "billing_queue")
    @Transactional
    public void handleCampaignSent(CampaignSentEvent event) {
        String username = event.getUsername() != null ? event.getUsername() : "testuser";
        BillingAccount account = accountRepository.findByUsername(username)
                .orElseGet(() -> {
                    BillingAccount newAcc = new BillingAccount();
                    newAcc.setUsername(username);
                    newAcc.setCredits(1000); // Default starting credits
                    return accountRepository.save(newAcc);
                });

        int cost = event.getRecipientCount();
        account.setCredits(Math.max(0, account.getCredits() - cost));
        accountRepository.save(account);

        BillingHistory history = new BillingHistory();
        history.setUsername(username);
        history.setAmount(cost);
        history.setType("DEDUCTION");
        history.setDescription("Campaign ID: " + event.getCampaignId());
        historyRepository.save(history);
        
        System.out.println("Deducted " + cost + " credits from " + username + ". Remaining: " + account.getCredits());

        // FR-N2: Alert users for low balance (below 100)
        if (account.getCredits() < 100) {
            java.util.Map<String, String> alert = new java.util.HashMap<>();
            alert.put("type", "LOW_BALANCE");
            alert.put("username", username);
            alert.put("message", "Your balance is low (" + account.getCredits() + "). Please recharge soon.");
            rabbitTemplate.convertAndSend("notification_exchange", "notification_routing_key", alert);
        }
    }
}
