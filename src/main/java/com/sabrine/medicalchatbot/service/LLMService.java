package com.sabrine.medicalchatbot.service;

import org.springframework.stereotype.Service;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class LLMService {

    public String generate(String prompt) {

        JSONObject body = new JSONObject();
        body.put("model", "mistral");
        body.put("prompt", prompt);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:11434/api/generate"))
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .header("Content-Type", "application/json")
                .build();

        try {
            HttpClient client = HttpClient.newHttpClient();
            String resp = client.send(req, HttpResponse.BodyHandlers.ofString()).body();
            JSONObject json = new JSONObject(resp);
            return json.getString("response");

        } catch (Exception e) {
            throw new RuntimeException("LLM error", e);
        }
    }
}
