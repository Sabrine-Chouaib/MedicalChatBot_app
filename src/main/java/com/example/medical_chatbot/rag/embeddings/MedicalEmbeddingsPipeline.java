package com.example.medical_chatbot.rag.embeddings;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.uuid.Generators;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.io.IOException;
import java.util.*;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.GlobalOpenTelemetry;

/**
 * Pipeline de génération d'embeddings médicaux via Ollama
 * Avec Distributed Tracing Jaeger
 */
public class MedicalEmbeddingsPipeline {


    /* =========================
       🔧 CONFIGURATION OLLAMA
       ========================= */

    private static String ollamaBaseUrl;
    private static final int EMBED_DIM = 768;
    private static final ObjectMapper mapper = new ObjectMapper();

    /* =========================
       🔁 GETTERS / SETTERS
       ========================= */

    public static void setOllamaBaseUrl(String url) {
        ollamaBaseUrl = url;
    }

    public static String getOllamaBaseUrl() {
        return ollamaBaseUrl;
    }

    /* =========================
       🧠 TRACER OPEN TELEMETRY
       ========================= */

    private static final Tracer tracer = GlobalOpenTelemetry.getTracer("medical-chatbot");

    /* =========================
       🧠 PIPELINE EMBEDDINGS
       ========================= */
    @WithSpan("Génération embeddings Ollama")
    public static List<Map<String, Object>> generateEmbeddings(
            List<String> chunks,
            String source
    ) throws IOException {

        List<Map<String, Object>> embeddingsList = new ArrayList<>();

        for (String chunkText : chunks) {
            // Span pour chaque chunk
            Span chunkSpan = tracer.spanBuilder("embedding-chunk").startSpan();
            chunkSpan.setAttribute("chunk.length", chunkText.length());
            try {
                List<Double> embedding = callOllamaEmbedding(chunkText);

                if (embedding.size() != EMBED_DIM) {
                    throw new RuntimeException(
                            "Dimension embedding incorrecte : " + embedding.size()
                    );
                }

                UUID id = Generators.nameBasedGenerator(
                        UUID.fromString("6ba7b810-9dad-11d1-80b4-00c04fd430c8")
                ).generate(chunkText);

                Map<String, Object> metadata = new HashMap<>();
                metadata.put("content", chunkText);
                metadata.put("length", chunkText.length());
                metadata.put("source", source);

                Map<String, Object> entry = new HashMap<>();
                entry.put("id", id.toString());
                entry.put("values", embedding);
                entry.put("metadata", metadata);

                embeddingsList.add(entry);
            } finally {
                chunkSpan.end();
            }
        }

        return embeddingsList;
    }

    /* =========================
       🌐 APPEL API OLLAMA AVEC TRACING
       ========================= */

    private static List<Double> callOllamaEmbedding(String text) throws IOException {

        // Span spécifique pour l'appel HTTP
        Span httpSpan = tracer.spanBuilder("ollama-http-call").startSpan();
        httpSpan.setAttribute("text.length", text != null ? text.length() : 0);

        try (CloseableHttpClient client = HttpClients.createDefault()) {

            HttpPost post = new HttpPost(ollamaBaseUrl);
            post.setHeader("Content-Type", "application/json");

            ObjectNode payload = mapper.createObjectNode();
            payload.put("model", "nomic-embed-text");
            payload.put("prompt", text);
            post.setEntity(new StringEntity(payload.toString()));

            return client.execute(post, response -> {
                int status = response.getCode();
                String responseBody =
                        new String(response.getEntity().getContent().readAllBytes());

                if (status != 200) {
                    throw new RuntimeException(
                            "Ollama HTTP error: " + status +
                                    "\nResponse body: " + responseBody
                    );
                }

                JsonNode root = mapper.readTree(responseBody);
                JsonNode embeddingNode = root.get("embedding");

                if (embeddingNode == null || !embeddingNode.isArray()) {
                    throw new RuntimeException(
                            "Réponse Ollama invalide : " + responseBody
                    );
                }

                List<Double> vector = new ArrayList<>();
                for (JsonNode v : embeddingNode) {
                    vector.add(v.asDouble());
                }
                return vector;
            });

        } finally {
            httpSpan.end();
        }
    }
}
