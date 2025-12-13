package com.example.medical_chatbot.rag.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

@Service
public class OllamaService {

    private final HttpClient httpClient;
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${ollama.url:http://localhost:11434}")
    private String ollamaUrl;

    public OllamaService(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public String generateAnswer(String prompt) throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("model", "phi2-local:latest");
        payload.put("prompt", prompt);
        payload.put("max_tokens", 256);

        String json = mapper.writeValueAsString(payload);

        // Création de la requête POST
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ollamaUrl + "/api/generate"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        // Exécution de la requête
        HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

        JsonNode root = mapper.readTree(response.body());

        if (root.has("result")) return root.get("result").asText();
        else if (root.has("text")) return root.get("text").asText();
        else return root.toString();
    }
}
