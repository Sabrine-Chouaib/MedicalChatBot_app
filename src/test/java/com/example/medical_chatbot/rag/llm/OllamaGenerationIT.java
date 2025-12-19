package com.example.medical_chatbot.rag.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class OllamaGenerationIT {

    @Test
    void shouldGenerateTextFromOllama() throws Exception {

        HttpClient client = HttpClient.newHttpClient();
        ObjectMapper mapper = new ObjectMapper();

        Map<String, Object> payload = Map.of(
                "model", "phi2-local:latestt",
                "prompt", "Bonjour, réponds en une phrase.",
                "stream", false
        );

        String json = mapper.writeValueAsString(payload);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:11434/api/generate"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        JsonNode body = mapper.readTree(response.body());

        System.out.println(" Ollama response:");
        System.out.println(body.toPrettyString());

        assertTrue(body.has("response"));
        assertFalse(body.get("response").asText().isBlank());
    }
}
