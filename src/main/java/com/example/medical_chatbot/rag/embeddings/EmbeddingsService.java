package com.example.medical_chatbot.rag.embeddings;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Service
public class EmbeddingsService {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${ollama.base-url:http://localhost:11434}")
    private String ollamaBaseUrl;
    

    public void setOllamaBaseUrl(String ollamaBaseUrl) {
        this.ollamaBaseUrl = ollamaBaseUrl;
        
    }
    
    public String getOllamaBaseUrl() {
          return ollamaBaseUrl;
    }


    /**
     * Production embeddings (Ollama)
     */
    public List<double[]> embedBatch(List<String> texts, String model) throws Exception {

        if (ollamaBaseUrl == null || !ollamaBaseUrl.startsWith("http")) {
            throw new IllegalStateException("Ollama base URL invalid: " + ollamaBaseUrl);
        }

        List<double[]> vectors = new ArrayList<>();

        for (String text : texts) {

            String payload = """
                {
                  "model": "%s",
                  "prompt": "%s"
                }
                """.formatted(model, text);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ollamaBaseUrl + "/api/embeddings"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            JsonNode root = mapper.readTree(response.body());
            JsonNode embeddingNode = root.get("embedding");

            double[] vector = new double[embeddingNode.size()];
            for (int i = 0; i < embeddingNode.size(); i++) {
                vector[i] = embeddingNode.get(i).asDouble();
            }

            vectors.add(vector);
        }
        return vectors;
    }

    /**
     * Test-only embedding (offline)
     */
    public float[] fakeEmbedding(String text) {
        float[] v = new float[256];
        for (int i = 0; i < v.length; i++) {
            v[i] = (float) Math.random();
        }
        return v;
    }
}
