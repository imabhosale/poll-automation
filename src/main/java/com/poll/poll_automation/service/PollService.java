package com.poll.poll_automation.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import java.util.HashMap;
import org.apache.hc.client5.http.classic.CloseableHttpClient;
import org.apache.hc.client5.http.classic.HttpClients;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
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
            // 🔹 Step 1: LOGIN (JWT)
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

            // 🔹 Step 4: LOGIN SESSION
            RestTemplate sessionTemplate = loginAndGetSessionTemplate();

// 🔹 Step 5: VOTE
            String voteUrl = "https://li1761-109.members.linode.com:8095/poll/chooseoption";

            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("id", String.valueOf(dinnerId));
            formData.add("option", "YES");

            HttpHeaders voteHeaders = new HttpHeaders();
            voteHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            voteHeaders.set("User-Agent", "Mozilla/5.0");

            HttpEntity<MultiValueMap<String, String>> voteRequest =
                    new HttpEntity<>(formData, voteHeaders);

            ResponseEntity<String> voteResponse =
                    sessionTemplate.postForEntity(voteUrl, voteRequest, String.class);

            System.out.println("✅ FINAL Vote response: " + voteResponse.getBody());
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }



    public RestTemplate loginAndGetSessionTemplate() {

        RestTemplate restTemplate = getRestTemplateWithCookies();

        String loginUrl = "https://li1761-109.members.linode.com:8095/signin";

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("email", email);
        body.add("password", password);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        headers.set("User-Agent", "Mozilla/5.0");
        headers.set("Origin", "https://li1761-109.members.linode.com:8095");
        headers.set("Referer", "https://li1761-109.members.linode.com:8095/signin");

        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<>(body, headers);

        ResponseEntity<String> response =
                restTemplate.postForEntity(loginUrl, request, String.class);

        System.out.println("✅ Session login done");

        return restTemplate; // 🔥 IMPORTANT (this holds cookies)
    }

    public List<String> loginWithSession() {

        RestTemplate restTemplate = new RestTemplate();

        String loginUrl = "https://li1761-109.members.linode.com:8095/signin";

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("email", email);
        body.add("password", password);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        headers.set("User-Agent", "Mozilla/5.0");
        headers.set("Origin", "https://li1761-109.members.linode.com:8095");
        headers.set("Referer", "https://li1761-109.members.linode.com:8095/signin");

        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<>(body, headers);

        ResponseEntity<String> response =
                restTemplate.postForEntity(loginUrl, request, String.class);

        List<String> cookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);

        System.out.println("🍪 Cookies: " + cookies);

        return cookies;
    }

    public RestTemplate getRestTemplateWithCookies() {

        BasicCookieStore cookieStore = new BasicCookieStore();

        CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setDefaultCookieStore(cookieStore)
                .build();

        HttpComponentsClientHttpRequestFactory factory =
                new HttpComponentsClientHttpRequestFactory(httpClient);

        return new RestTemplate(factory);
    }
}
