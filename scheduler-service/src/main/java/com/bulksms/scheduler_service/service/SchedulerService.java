package com.bulksms.scheduler_service.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;
import java.util.Arrays;

@Service
public class SchedulerService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Scheduled(fixedRate = 60000) // Check every minute
    public void processScheduledCampaigns() {
        System.out.println("Scheduler: Checking for pending campaigns at " + LocalDateTime.now());
        
        try {
            // Fetch all campaigns
            Map[] campaigns = restTemplate.getForObject("http://CAMPAIGN-SERVICE/api/campaigns", Map[].class);
            if (campaigns == null) return;

            LocalDateTime now = LocalDateTime.now();

            for (Map campaign : campaigns) {
                String status = (String) campaign.get("status");
                String schedule = (String) campaign.get("schedule");

                if ("scheduled".equals(status) && schedule != null && !schedule.isEmpty()) {
                    LocalDateTime scheduledTime = LocalDateTime.parse(schedule);
                    if (scheduledTime.isBefore(now)) {
                        System.out.println("Scheduler: Triggering campaign " + campaign.get("id"));
                        restTemplate.postForObject("http://CAMPAIGN-SERVICE/api/campaigns/" + campaign.get("id") + "/dispatch", null, Map.class);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Scheduler error: " + e.getMessage());
        }
    }
}
