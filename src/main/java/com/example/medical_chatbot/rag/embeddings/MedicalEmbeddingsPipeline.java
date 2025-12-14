package com.example.medical_chatbot.rag.embeddings;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.uuid.Generators;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.io.IOException;
import java.util.*;

/**
 * Pipeline de génération d'embeddings médicaux via Ollama
 * Utilisé dans le cadre du RAG (Retrieval Augmented Generation)
 */
public class MedicalEmbeddingsPipeline {

    /* =========================
       🔧 CONFIGURATION OLLAMA
       ========================= */

    // ✅ Endpoint natif Ollama
    private static String ollamaBaseUrl =
            "http://localhost:11434/api/embeddings";

    // Dimension attendue des embeddings
    private static final int EMBED_DIM = 768;

    // Mapper JSON partagé
    private static final ObjectMapper mapper = new ObjectMapper();


    /* =========================
       🔁 GETTERS / SETTERS
       ========================= */

    public static void setOllamaBaseUrl(String url) {
        ollamaBaseUrl = url;
    }

    // 🔹 Utilisé par RagServiceLLM
    public static String getOllamaBaseUrl() {
        return ollamaBaseUrl;
    }


    /* =========================
       🧠 PIPELINE EMBEDDINGS
       ========================= */

    /**
     * Génère les embeddings pour une liste de chunks médicaux
     *
     * @param chunks Liste de chunks textuels
     * @param source Source du document (PDF, OCR, etc.)
     * @return Liste de vecteurs prêts pour Pinecone
     */
    public static List<Map<String, Object>> generateEmbeddings(
            List<String> chunks,
            String source
    ) throws IOException {

        List<Map<String, Object>> embeddingsList = new ArrayList<>();

        for (String chunkText : chunks) {

            /* 1️⃣ Appel Ollama pour UN chunk */
            List<Double> embedding = callOllamaEmbedding(chunkText);

            if (embedding.size() != EMBED_DIM) {
                throw new RuntimeException(
                        "Dimension embedding incorrecte : " + embedding.size()
                );
            }

            /* 2️⃣ Génération UUID déterministe (basé sur le contenu) */
            UUID id = Generators.nameBasedGenerator(
                    UUID.fromString("6ba7b810-9dad-11d1-80b4-00c04fd430c8")
            ).generate(chunkText);

            /* 3️⃣ Métadonnées */
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("content", chunkText);     // 🔴 TEXTE DU CHUNK (OBLIGATOIRE)
            metadata.put("length", chunkText.length());
            metadata.put("source", source);

            /* 4️⃣ Entrée finale Pinecone */
            Map<String, Object> entry = new HashMap<>();
            entry.put("id", id.toString());
            entry.put("values", embedding);
            entry.put("metadata", metadata);

            embeddingsList.add(entry);
        }

        return embeddingsList;
    }


    /* =========================
       🌐 APPEL API OLLAMA
       ========================= */

    /**
     * Appel à l'API Ollama (/api/embeddings) pour un seul chunk
     *
     * @param text Texte à encoder
     * @return Vecteur d'embedding
     */
    private static List<Double> callOllamaEmbedding(String text) throws IOException {

        try (CloseableHttpClient client = HttpClients.createDefault()) {

            HttpPost post = new HttpPost(ollamaBaseUrl);
            post.setHeader("Content-Type", "application/json");

            // ✅ Format requis par Ollama
            ObjectNode payload = mapper.createObjectNode();
            payload.put("model", "nomic-embed-text");
            payload.put("prompt", text);

            post.setEntity(new StringEntity(payload.toString()));

            return client.execute(post, response -> {

                int status = response.getCode();
                String responseBody =
                        new String(response.getEntity().getContent().readAllBytes());

                // 🔒 Gestion erreurs HTTP
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
        }
    }
}
