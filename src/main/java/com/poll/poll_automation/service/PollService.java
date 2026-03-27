package com.poll.poll_automation.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PollService {

    @Value("${poll.email}")
    private String email;

    @Value("${poll.password}")
    private String password;

    @Autowired
    private AuthService authService;

    private final String BASE_API = "https://li1761-109.members.linode.com:8096";

    public void vote() {

        RestTemplate restTemplate = new RestTemplate();

        try {
            // 🔹 Step 1: LOGIN
            String token = authService.loginAndGetToken(email, password);

            if (token == null) {
                System.out.println("❌ Login failed");
                return;
            }

            System.out.println("✅ Token: " + token);

            // 🔹 Step 2: GET POLLS
            String pollUrl = BASE_API + "/api/v1/poll/getpolls";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);

            HttpEntity<String> request = new HttpEntity<>("{}", headers);

            ResponseEntity<Map> response =
                    restTemplate.postForEntity(pollUrl, request, Map.class);

            Map<String, Object> body = response.getBody();

            System.out.println("Polls response: " + body);

            List<Map<String, Object>> polls =
                    (List<Map<String, Object>>) body.get("content");

            if (polls == null) {
                System.out.println("❌ Poll list is null");
                return;
            }

            // 🔹 Step 3: Find Dinner poll
            Integer dinnerId = null;

            for (Map<String, Object> poll : polls) {
                String title = (String) poll.get("title");

                if (title != null && title.equalsIgnoreCase("Dinner")) {
                    dinnerId = (Integer) poll.get("id");
                    break;
                }
            }

            if (dinnerId == null) {
                System.out.println("❌ Dinner poll not found");
                return;
            }

            System.out.println("✅ Dinner ID: " + dinnerId);

            // 🔹 Step 4: GET POLL DETAILS (IMPORTANT)
            String detailsUrl = BASE_API + "/api/v1/poll/details?id=" + dinnerId;

            HttpHeaders detailsHeaders = new HttpHeaders();
            detailsHeaders.setBearerAuth(token);

            HttpEntity<String> detailsRequest = new HttpEntity<>(detailsHeaders);

            ResponseEntity<Map> detailsResponse =
                    restTemplate.exchange(detailsUrl, HttpMethod.GET, detailsRequest, Map.class);

            Map<String, Object> detailsBody = detailsResponse.getBody();

            System.out.println("Poll Details: " + detailsBody);

            // 🔹 Step 5: Extract OPTION ID (Yes)

            Map<String, Object> pollObj =
                    (Map<String, Object>) detailsBody.get("poll");

            if (pollObj == null) {
                System.out.println("❌ poll object not found");
                return;
            }

            List<Map<String, Object>> options =
                    (List<Map<String, Object>>) pollObj.get("options");

            if (options == null) {
                System.out.println("❌ options not found inside poll");
                return;
            }

            Integer optionId = null;

            for (Map<String, Object> opt : options) {
                String text = (String) opt.get("optionText");

                if (text != null && text.equalsIgnoreCase("Yes")) {
                    optionId = (Integer) opt.get("id");
                    break;
                }
            }

            if (optionId == null) {
                System.out.println("❌ YES option not found");
                return;
            }

            System.out.println("✅ Option ID: " + optionId);

            // 🔹 Step 6: SAVE VOTE (FINAL FIX)

            String voteUrl = BASE_API + "/api/v1/vote/save";

// 🔥 EXACT payload from browser
            Map<String, Object> voteBody = new HashMap<>();
            voteBody.put("pollId", dinnerId);
            voteBody.put("selectedOptionIds", List.of(optionId)); // ✅ exact key
            voteBody.put("userId", 294); // 🔥 IMPORTANT

            HttpHeaders voteHeaders = new HttpHeaders();
            voteHeaders.setContentType(MediaType.APPLICATION_JSON);
            voteHeaders.setBearerAuth(token);

            voteHeaders.set("Origin", "https://li1761-109.members.linode.com:8095");
            voteHeaders.set("Referer", "https://li1761-109.members.linode.com:8095/");

            HttpEntity<Map<String, Object>> voteRequest =
                    new HttpEntity<>(voteBody, voteHeaders);

            ResponseEntity<String> voteResponse =
                    restTemplate.postForEntity(voteUrl, voteRequest, String.class);

            System.out.println("🔥 FINAL Vote response: " + voteResponse.getBody());
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }
}