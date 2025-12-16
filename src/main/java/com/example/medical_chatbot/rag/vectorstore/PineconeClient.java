package com.example.medical_chatbot.rag.vectorstore;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class PineconeClient {

    private final String apiKey;
    private final String host;  // Utilisation du host complet
    private static final ObjectMapper mapper = new ObjectMapper();

    public PineconeClient(String apiKey, String host) {
        this.apiKey = apiKey;
        this.host = host;

    }

     // ✅ Getter pour host
    public String getHost() {
        return host;
    }

    // ✅ Getter pour apiKey
    public String getApiKey() {
        return apiKey;
    }

    /**
     * Upsert des embeddings dans Pinecone
     */
    @WithSpan("Chunking médical")
    public void upsertEmbeddings(List<Map<String, Object>> embeddingsList) throws IOException {
        String url = host + "/vectors/upsert"; // host complet + endpoint

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(url);
            post.setHeader("Content-Type", "application/json");
            post.setHeader("Api-Key", apiKey);

            ObjectNode payload = mapper.createObjectNode();
            payload.putArray("vectors").addAll(
                    embeddingsList.stream().map(entry -> {
                        ObjectNode node = mapper.createObjectNode();
                        node.put("id", entry.get("id").toString());
                        node.putPOJO("values", entry.get("values"));
                        node.putPOJO("metadata", entry.get("metadata"));
                        return node;
                    }).toList()
            );

            post.setEntity(new StringEntity(payload.toString()));

            client.execute(post, response -> {
                int status = response.getCode();
                String responseBody = new String(response.getEntity().getContent().readAllBytes());
                if (status != 200) {
                    throw new RuntimeException("Pinecone upsert error: " + status + "\n" + responseBody);
                }
                System.out.println("✅ Upsert Pinecone réussi : " + responseBody);
                return null;
            });
        }
    }
}
