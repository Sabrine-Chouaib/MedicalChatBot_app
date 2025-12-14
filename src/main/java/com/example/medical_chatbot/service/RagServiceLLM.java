package com.example.medical_chatbot.service;

import com.example.medical_chatbot.rag.vectorstore.PineconeStore;
import com.example.medical_chatbot.rag.prompt.SystemPrompt;
import com.example.medical_chatbot.rag.embeddings.MedicalEmbeddingsPipeline;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RagServiceLLM {

    private final PineconeStore pineconeStore;
    private final SystemPrompt systemPrompt;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RagServiceLLM(
            PineconeStore pineconeStore,
            SystemPrompt systemPrompt
    ) {
        this.pineconeStore = pineconeStore;
        this.systemPrompt = systemPrompt;
    }

    /**
 * Recherche dans Pinecone + génération de réponse LLM Ollama
 * Adaptée pour Ollama API avec stream:false
 */
public String generateAnswerWithLLM(String question, int topK) throws Exception {

    // 1️⃣ Génération embedding pour la question
    List<Map<String, Object>> embeddings = MedicalEmbeddingsPipeline.generateEmbeddings(
            List.of(question), "question"
    );
    @SuppressWarnings("unchecked")
    double[] vector = ((List<Double>) embeddings.get(0).get("values"))
            .stream().mapToDouble(Double::doubleValue).toArray();

    // 2️⃣ Recherche dans Pinecone
    JsonNode searchResult = pineconeStore.query(vector, topK);

     // 3️⃣ Récupération du contexte depuis Pinecone (ROBUSTE)
StringBuilder retrievedContext = new StringBuilder();

if (searchResult != null && searchResult.has("matches")) {
    for (JsonNode match : searchResult.get("matches")) {

        JsonNode metadata = match.get("metadata");
        if (metadata == null) continue;

        JsonNode contentNode = metadata.get("content");
        if (contentNode == null || contentNode.asText().isBlank()) continue;

        retrievedContext.append(contentNode.asText()).append("\n");
    }
}


    // 4️⃣ Construction du prompt
    String prompt = systemPrompt.buildPrompt(question, retrievedContext.toString());

    // 5️⃣ Préparation payload Ollama avec stream:false
    Map<String, Object> payload = new HashMap<>();
    payload.put("model", "phi2-local:latest");
    payload.put("prompt", prompt);
    payload.put("max_tokens", 200);
    payload.put("stream", false); // ✅ clé importante pour obtenir la réponse complète

    String jsonPayload = objectMapper.writeValueAsString(payload);

    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(MedicalEmbeddingsPipeline.getOllamaBaseUrl() + "/api/generate"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
            .build();

    HttpResponse<String> response =
        httpClient.send(request, HttpResponse.BodyHandlers.ofString());

JsonNode responseJson = objectMapper.readTree(response.body());

// 🔥 LOG DE SÉCURITÉ (temporaire mais crucial)
System.out.println("📦 Réponse brute Ollama :");
System.out.println(responseJson.toPrettyString());

// ✅ EXTRACTION CORRECTE POUR OLLAMA
JsonNode responseNode = responseJson.get("response");

if (responseNode == null || responseNode.asText().isBlank()) {
    throw new IllegalStateException(
        "Réponse Ollama invalide ou vide : " + responseJson.toPrettyString()
    );
}

return responseNode.asText().trim();


}

}



