package com.example.medical_chatbot.rag.vectorstore;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * PineconeStore : wrapper simple autour de PineconeClient
 * Fournit uniquement la méthode query pour récupérer les topK vecteurs depuis Pinecone
 */
public class PineconeStore {

    private final PineconeClient client;
    private static final ObjectMapper mapper = new ObjectMapper();

    public PineconeStore(String apiKey, String host) {
        this.client = new PineconeClient(apiKey, host);
    }

    /**
     * Query Pinecone pour récupérer topK vecteurs similaires
     */
    public JsonNode query(double[] vector, int topK) throws IOException {
        String url = client.getHost() + "/query";

        var payloadNode = mapper.createObjectNode();
        payloadNode.put("topK", topK);
        var vectorArray = payloadNode.putArray("vector");
        for (double v : vector) {
            vectorArray.add(v);
        }

        try (var httpClient = org.apache.hc.client5.http.impl.classic.HttpClients.createDefault()) {
            var post = new org.apache.hc.client5.http.classic.methods.HttpPost(url);
            post.setHeader("Content-Type", "application/json");
            post.setHeader("Api-Key", client.getApiKey());
            post.setEntity(new org.apache.hc.core5.http.io.entity.StringEntity(payloadNode.toString()));

            return httpClient.execute(post, response -> {
                int status = response.getCode();
                String responseBody = new String(response.getEntity().getContent().readAllBytes());
                if (status != 200) {
                    throw new RuntimeException("Pinecone query error: " + status + "\n" + responseBody);
                }
                return mapper.readTree(responseBody);
            });
        }
    }

   
}
