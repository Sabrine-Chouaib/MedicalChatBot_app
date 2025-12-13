package com.example.medical_chatbot.rag.vectorstore;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class PineconeStoreIT {

    @Test
    void shouldUpsertAndQueryFakeVector768() throws Exception {

        // 1️⃣ Init store (sans Spring)
        PineconeStore store = new PineconeStore(HttpClient.newHttpClient());

        store.setApiKey("pcsk_2dpnua_DzrGwtVy5ScmuRB2ZKWyKEXnvJQq835YxCzvGvws7uKJVFT2V7BFqX3fjM8L7io");
        store.setHost("https://medical-chatbot-ollama-ndr6ggc.svc.aped-4627-b74a.pinecone.io");
        

        // 2️⃣ Faux vecteur 768D
        double[] vector = new double[768];
        for (int i = 0; i < 768; i++) {
            vector[i] = Math.random();
        }

        // 3️⃣ Préparer upsert
        Map<String, Object> entry = new HashMap<>();
        entry.put("id", "java-test-" + System.currentTimeMillis());
        entry.put("values", vector);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("text", "Test Pinecone Java SEUL");
        entry.put("metadata", metadata);

        boolean upsertOk = store.upsert(List.of(entry));

        // 4️⃣ Vérification upsert
        assertTrue(upsertOk);
        System.out.println("✅ Upsert Pinecone OK");

        // 5️⃣ Query
        JsonNode res = store.query(vector, 1);

        assertNotNull(res);
        assertTrue(res.has("matches"));

        System.out.println("✅ Query result = " + res.toPrettyString());
    }
}
