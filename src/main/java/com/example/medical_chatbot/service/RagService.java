package com.example.medical_chatbot.service;

import com.example.medical_chatbot.rag.embeddings.EmbeddingsService;
import com.example.medical_chatbot.rag.vectorstore.PineconeStore;
import com.example.medical_chatbot.rag.prompt.SystemPrompt;
import com.example.medical_chatbot.rag.pdf.PDFChunker;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RagService {

    private final EmbeddingsService embeddingsService;
    private final PineconeStore pineconeStore;
    private final SystemPrompt systemPrompt;
    private final PDFChunker pdfChunker;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RagService(
            EmbeddingsService embeddingsService,
            PineconeStore pineconeStore,
            SystemPrompt systemPrompt,
            PDFChunker pdfChunker
    ) {
        this.embeddingsService = embeddingsService;
        this.pineconeStore = pineconeStore;
        this.systemPrompt = systemPrompt;
        this.pdfChunker = pdfChunker;
    }

    // ==========================
    // INDEXER UNE PHRASE
    // ==========================
    public void indexText(String text) throws Exception {
        if (text == null || text.isBlank()) return;

        List<double[]> embeddings = embeddingsService.embedBatch(List.of(text), "nomic-embed-text");
        double[] vector = embeddings.get(0);

        Map<String, Object> entry = new HashMap<>();
        entry.put("id", UUID.randomUUID().toString());
        entry.put("values", vector);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("text", text);
        entry.put("metadata", metadata);

        boolean ok = pineconeStore.upsert(List.of(entry));
        if (!ok) throw new IllegalStateException("Upsert Pinecone échoué");

        System.out.println("✅ Texte indexé dans Pinecone");
    }

    // ==========================
    // INDEXER UN PDF EN CHUNKS
    // ==========================
    public void indexPDF(String pdfPath) throws Exception {
        for (String chunk : pdfChunker.extractTextChunksStream(pdfPath)) {
            if (chunk == null || chunk.isBlank()) continue;
            indexText(chunk);
        }
        System.out.println("✅ PDF indexé en streaming avec TextChunker et Pinecone");
    }

    // ==========================
    // RECHERCHE DANS PINECONE
    // ==========================
    public JsonNode search(String question, int topK) throws Exception {
        double[] vector = embeddingsService.embedBatch(List.of(question), "nomic-embed-text").get(0);
        return pineconeStore.query(vector, topK);
    }

    // ==========================
    // GENERATION DE REPONSE AVEC OLLAMA
    // ==========================
    public String generateAnswer(String question, int topK) throws Exception {
        JsonNode searchResult = search(question, topK);

        StringBuilder retrievedContext = new StringBuilder();
        if (searchResult.has("matches")) {
            for (JsonNode match : searchResult.get("matches")) {
                retrievedContext.append(match.get("metadata").get("text").asText()).append("\n");
            }
        }

        String prompt = systemPrompt.buildPrompt(question, retrievedContext.toString());

        Map<String, Object> payload = new HashMap<>();
        payload.put("model", "phi2-local:latest");
        payload.put("prompt", prompt);
        payload.put("max_tokens", 200);

        String jsonPayload = objectMapper.writeValueAsString(payload);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(embeddingsService.getOllamaBaseUrl() + "/v1/completions"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        JsonNode responseJson = objectMapper.readTree(response.body());

        JsonNode choices = responseJson.get("choices");
        if (choices != null && choices.isArray() && choices.size() > 0) {
            return choices.get(0).get("text").asText();
        } else {
            throw new IllegalStateException("Réponse Ollama invalide : " + responseJson.toPrettyString());
        }
    }
}
