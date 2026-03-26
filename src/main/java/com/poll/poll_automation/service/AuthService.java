package com.poll.poll_automation.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
@Service
public class AuthService {

    private final String BASE_API = "https://li1761-109.members.linode.com:8096";

    public String loginAndGetToken(String email, String password) {

        RestTemplate restTemplate = new RestTemplate();

        String url = BASE_API + "/api/v1/auth/login";

        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        headers.set("Origin", "https://li1761-109.members.linode.com:8095");
        headers.set("Referer", "https://li1761-109.members.linode.com:8095/");
        headers.set("User-Agent", "Mozilla/5.0");

        HttpEntity<Map<String, String>> request =
                new HttpEntity<>(body, headers);

        ResponseEntity<Map> response =
                restTemplate.postForEntity(url, request, Map.class);

        System.out.println("Login response: " + response.getBody());

        // ✅ FIXED
        String token = (String) response.getBody().get("jwtToken");

        return token;
    }


}