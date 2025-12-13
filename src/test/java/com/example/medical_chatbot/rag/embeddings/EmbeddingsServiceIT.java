package com.example.medical_chatbot.rag.embeddings;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EmbeddingsServiceIT {

    @Test
    void shouldGenerateRealEmbeddingFromOllama() throws Exception {

        EmbeddingsService service = new EmbeddingsService();
         // ✅ injection manuelle (hors Spring)
    service.setOllamaBaseUrl("http://localhost:11434");

        List<double[]> vectors =
                service.embedBatch(List.of("Bonjour"), "nomic-embed-text");

        assertNotNull(vectors);
        assertEquals(1, vectors.size());
        assertEquals(768, vectors.get(0).length);

        System.out.println("✅ Embedding dimension = " + vectors.get(0).length);
    }
}
