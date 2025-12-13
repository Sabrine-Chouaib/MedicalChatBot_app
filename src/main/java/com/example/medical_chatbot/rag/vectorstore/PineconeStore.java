package com.example.medical_chatbot.rag.vectorstore;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PineconeStore {

    private final HttpClient httpClient;
    private final ObjectMapper mapper = new ObjectMapper();

    // 🔥 valeurs runtime (tests ou overrides)
    private String apiKey;
    private String host;

    // 🔹 valeurs Spring (prod)
    @Value("${pinecone.api.key:}")
    private String pineconeApiKey;

    @Value("${pinecone.host:}")
    private String pineconeHost;

    @Value("${pinecone.index.name:}")
    private String pineconeIndexName; // (optionnel, pas utilisé dans l’URL)

    public PineconeStore(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /* ==========================
       SETTERS (tests / non-Spring)
       ========================== */

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setIndexName(String indexName) {
        this.pineconeIndexName = indexName;
    }

    /* ==========================
       RESOLUTION DES VALEURS
       ========================== */

    private String resolvedHost() {
        String h = (host != null && !host.isBlank()) ? host : pineconeHost;
        if (h == null || !h.startsWith("http")) {
            throw new IllegalStateException(
                "Pinecone host invalide (doit commencer par https://)"
            );
        }
        return h;
    }

    private String resolvedApiKey() {
        return (apiKey != null && !apiKey.isBlank()) ? apiKey : pineconeApiKey;
    }

    /* ==========================
       UPSERT
       ========================== */

    public boolean upsert(List<Map<String, Object>> vectors) throws Exception {

        String url = resolvedHost() + "/vectors/upsert";

        Map<String, Object> body = new HashMap<>();
        body.put("vectors", vectors);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Api-Key", resolvedApiKey())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                        mapper.writeValueAsString(body)))
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Pinecone UPSERT status = " + response.statusCode());
        System.out.println(response.body());

        return response.statusCode() >= 200 && response.statusCode() < 300;
    }

    /* ==========================
       QUERY
       ========================== */

    public JsonNode query(double[] vector, int topK) throws Exception {

        String url = resolvedHost() + "/query";

        Map<String, Object> body = new HashMap<>();
        body.put("vector", vector);
        body.put("topK", topK);
        body.put("includeMetadata", true);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Api-Key", resolvedApiKey())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                        mapper.writeValueAsString(body)))
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        return mapper.readTree(response.body());
    }
}
