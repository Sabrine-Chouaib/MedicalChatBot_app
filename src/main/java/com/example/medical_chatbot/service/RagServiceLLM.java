package com.example.medical_chatbot.service;

import com.example.medical_chatbot.rag.embeddings.MedicalEmbeddingsPipeline;
import com.example.medical_chatbot.rag.prompt.SystemPrompt;
import com.example.medical_chatbot.rag.vectorstore.PineconeStore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.instrumentation.annotations.WithSpan;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service RAG avec LLM (Ollama)
 *
 * Responsabilités :
 *  - Générer l'embedding de la question
 *  - Interroger Pinecone (similarité vectorielle)
 *  - Construire le prompt final
 *  - Appeler Ollama (stream:false)
 */
public class RagServiceLLM {

    /* =====================================================
       🔧 DÉPENDANCES
       ===================================================== */

    private final PineconeStore pineconeStore;
    private final SystemPrompt systemPrompt;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();


    /* =====================================================
       🏗️ CONSTRUCTEUR
       ===================================================== */

    public RagServiceLLM(PineconeStore pineconeStore,
                         SystemPrompt systemPrompt) {
        this.pineconeStore = pineconeStore;
        this.systemPrompt = systemPrompt;
    }


    /* =====================================================
       🧠 MÉTHODE PRINCIPALE
       ===================================================== */

    /**
     * Pipeline RAG complet :
     *  1. Embedding question
     *  2. Recherche Pinecone
     *  3. Construction du contexte
     *  4. Prompt final
     *  5. Appel Ollama
     *
     * @param question question utilisateur
     * @param topK     nombre de chunks à récupérer
     * @return réponse générée par le LLM
     */
    @WithSpan("LLM Call")
    public String generateAnswerWithLLM(String question, int topK) throws Exception {

        /* =============================
           1️⃣ EMBEDDING DE LA QUESTION
           ============================= */

        List<Map<String, Object>> embeddings =
                MedicalEmbeddingsPipeline.generateEmbeddings(
                        List.of(question), "question"
                );

        @SuppressWarnings("unchecked")
        double[] queryVector = ((List<Double>) embeddings.get(0).get("values"))
                .stream()
                .mapToDouble(Double::doubleValue)
                .toArray();


        /* =============================
           2️⃣ RECHERCHE DANS PINECONE
           ============================= */

        JsonNode searchResult = pineconeStore.query(queryVector, topK);


        /* =============================
           3️⃣ CONSTRUCTION DU CONTEXTE
           ============================= */

        StringBuilder retrievedContext = new StringBuilder();

        if (searchResult != null && searchResult.has("matches")) {
            for (JsonNode match : searchResult.get("matches")) {

                JsonNode metadata = match.get("metadata");
                if (metadata == null) continue;

                JsonNode contentNode = metadata.get("content");
                if (contentNode == null || contentNode.asText().isBlank()) continue;

                retrievedContext
                        .append(contentNode.asText())
                        .append("\n");
            }
        }


        /* =============================
           4️⃣ PROMPT FINAL
           ============================= */

        String prompt = systemPrompt.buildPrompt(
                question,
                retrievedContext.toString()
        );


        /* =============================
           5️⃣ APPEL OLLAMA (stream:false)
           ============================= */

        Map<String, Object> payload = new HashMap<>();
        payload.put("model", "phi");
        payload.put("prompt", prompt);
        payload.put("stream", false); // important pour réponse complète

        String jsonPayload = objectMapper.writeValueAsString(payload);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:11434/api/generate"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());


        /* =============================
           6️⃣ LECTURE DE LA RÉPONSE
           ============================= */

        JsonNode responseJson = objectMapper.readTree(response.body());

        // LOG DEBUG (à retirer en prod)
        System.out.println("📦 Réponse brute Ollama :");
        System.out.println(responseJson.toPrettyString());

        JsonNode responseNode = responseJson.path("response");

        if (responseNode.isMissingNode() || responseNode.asText().isBlank()) {
            throw new IllegalStateException(
                    "Réponse Ollama invalide : " + responseJson.toPrettyString()
            );
        }

        return responseNode.asText().trim();
    }
}
