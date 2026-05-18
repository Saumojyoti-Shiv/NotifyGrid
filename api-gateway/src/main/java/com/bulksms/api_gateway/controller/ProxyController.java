package com.bulksms.api_gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Collections;

@RestController
@RequestMapping("/api")
public class ProxyController {

    @Autowired
    private RestTemplate restTemplate;

    @RequestMapping("/**")
    public ResponseEntity<String> proxy(HttpServletRequest request, @RequestBody(required = false) String body) {
        String path = request.getRequestURI(); // e.g. /api/auth/login
        String serviceName = getServiceName(path);
        
        if (serviceName == null) {
            return ResponseEntity.notFound().build();
        }

        String targetUrl = "http://" + serviceName + path;
        
        HttpHeaders headers = new HttpHeaders();
        Collections.list(request.getHeaderNames()).forEach(headerName -> 
            headers.add(headerName, request.getHeader(headerName))
        );
        
        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        
        try {
            return restTemplate.exchange(targetUrl, HttpMethod.valueOf(request.getMethod()), entity, String.class);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Proxy Error: " + e.getMessage());
        }
    }

    private String getServiceName(String path) {
        if (path.startsWith("/api/auth")) return "AUTH-SERVICE";
        if (path.startsWith("/api/contacts")) return "CONTACT-SERVICE";
        if (path.startsWith("/api/campaigns")) return "CAMPAIGN-SERVICE";
        if (path.startsWith("/api/messages")) return "MESSAGING-SERVICE";
        if (path.startsWith("/api/delivery")) return "DELIVERY-REPORT-SERVICE";
        if (path.startsWith("/api/billing")) return "BILLING-SERVICE";
        if (path.startsWith("/api/scheduler")) return "SCHEDULER-SERVICE";
        return null;
    }
}
