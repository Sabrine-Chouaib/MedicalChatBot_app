package com.example.medical_chatbot.service;

import com.example.medical_chatbot.rag.embeddings.MedicalEmbeddingsPipeline;
import com.example.medical_chatbot.rag.prompt.SystemPrompt;
import com.example.medical_chatbot.rag.vectorstore.PineconeStore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RagServiceLLM {

    private final PineconeStore pineconeStore;
    private final SystemPrompt systemPrompt;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${llm.api.url}")
    private String llmApiUrl;

    public RagServiceLLM(PineconeStore pineconeStore,
                         SystemPrompt systemPrompt) {
        this.pineconeStore = pineconeStore;
        this.systemPrompt = systemPrompt;
    }

    public String generateAnswerWithLLM(String question, int topK) {
        try {
            // 1️⃣ Embedding
            List<Map<String, Object>> embeddings =
                    MedicalEmbeddingsPipeline.generateEmbeddings(List.of(question), "question");

            @SuppressWarnings("unchecked")
            double[] queryVector = ((List<Double>) embeddings.get(0).get("values"))
                    .stream()
                    .mapToDouble(Double::doubleValue)
                    .toArray();

            // 2️⃣ Recherche Pinecone
            JsonNode searchResult = pineconeStore.query(queryVector, topK);

            // 3️⃣ Contexte
            StringBuilder retrievedContext = new StringBuilder();
            if (searchResult != null && searchResult.has("matches")) {
                for (JsonNode match : searchResult.get("matches")) {
                    JsonNode metadata = match.get("metadata");
                    if (metadata != null) {
                        JsonNode contentNode = metadata.get("content");
                        if (contentNode != null && !contentNode.asText().isBlank()) {
                            retrievedContext.append(contentNode.asText()).append("\n");
                        }
                    }
                }
            }

            // 4️⃣ Prompt final
            String prompt = systemPrompt.buildPrompt(question, retrievedContext.toString());

            // 5️⃣ Appel Ollama
            Map<String, Object> payload = new HashMap<>();
            payload.put("model", "tinyllama:latest");
            payload.put("prompt", prompt);
            payload.put("stream", false);

            String jsonPayload = objectMapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(llmApiUrl + "/api/generate")) // ✅ utilise variable
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                return "❌ Erreur LLM : " + response.statusCode() + " - " + response.body();
            }

            JsonNode responseJson = objectMapper.readTree(response.body());
            JsonNode responseNode = responseJson.path("response");

            if (responseNode.isMissingNode() || responseNode.asText().isBlank()) {
                return "❌ Réponse Ollama invalide : " + responseJson.toPrettyString();
            }

            return responseNode.asText().trim();

        } catch (Exception e) {
            e.printStackTrace();
            return "❌ Erreur lors de la génération : " + e.getMessage();
        }
    }
}
