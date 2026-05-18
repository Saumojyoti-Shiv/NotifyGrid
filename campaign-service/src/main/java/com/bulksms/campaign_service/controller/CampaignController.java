package com.bulksms.campaign_service.controller;

import com.bulksms.campaign_service.dto.ContactDto;
import com.bulksms.campaign_service.dto.GroupDto;
import com.bulksms.campaign_service.dto.SmsMessagePayload;
import com.bulksms.campaign_service.model.Campaign;
import com.bulksms.campaign_service.repository.CampaignRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/campaigns")
public class CampaignController {

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping
    public List<Campaign> getAllCampaigns() {
        return campaignRepository.findAll();
    }

    @PostMapping
    public Campaign createCampaign(@RequestBody Campaign campaign) {
        Campaign saved = campaignRepository.save(campaign);
        if ("sent".equals(saved.getStatus())) {
            dispatch(saved);
        }
        return saved;
    }

    @PostMapping("/{id}/dispatch")
    public Campaign dispatchCampaign(@PathVariable Long id) {
        Campaign campaign = campaignRepository.findById(id).orElseThrow();
        dispatch(campaign);
        campaign.setStatus("sent");
        return campaignRepository.save(campaign);
    }

    private void dispatch(Campaign campaign) {
        List<ContactDto> contacts = new ArrayList<>();
        if ("all".equals(campaign.getGroupId())) {
            ContactDto[] response = restTemplate.getForObject("http://CONTACT-SERVICE/api/contacts", ContactDto[].class);
            if (response != null) contacts = Arrays.asList(response);
        } else {
            try {
                Long groupId = Long.parseLong(campaign.getGroupId());
                GroupDto group = restTemplate.getForObject("http://CONTACT-SERVICE/api/contacts/groups/" + groupId, GroupDto.class);
                if (group != null && group.getContacts() != null) {
                    contacts = group.getContacts();
                }
            } catch (NumberFormatException e) {
                // Ignore or handle
            }
        }

        for (ContactDto contact : contacts) {
            SmsMessagePayload payload = new SmsMessagePayload(campaign.getId(), contact.getPhone(), campaign.getMessage());
            rabbitTemplate.convertAndSend("sms_exchange", "sms_routing_key", payload);
        }

        // FR-B2: Deduct credits per message sent (via Billing Service)
        java.util.Map<String, Object> billingEvent = new java.util.HashMap<>();
        billingEvent.put("campaignId", campaign.getId());
        billingEvent.put("recipientCount", contacts.size());
        billingEvent.put("username", campaign.getCreatedBy() != null ? campaign.getCreatedBy() : "testuser");
        rabbitTemplate.convertAndSend("billing_exchange", "billing_routing_key", billingEvent);

        // FR-N1: Notify users about campaign status
        java.util.Map<String, String> notification = new java.util.HashMap<>();
        notification.put("type", "CAMPAIGN_LAUNCHED");
        notification.put("username", campaign.getCreatedBy() != null ? campaign.getCreatedBy() : "testuser");
        notification.put("message", "Your campaign '" + campaign.getName() + "' has been launched to " + contacts.size() + " recipients.");
        rabbitTemplate.convertAndSend("notification_exchange", "notification_routing_key", notification);

        campaign.setRecipientCount(contacts.size());
        campaign.setSentAt(LocalDateTime.now().toString());
        campaignRepository.save(campaign);
    }
}
