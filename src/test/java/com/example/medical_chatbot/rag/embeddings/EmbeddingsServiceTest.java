package com.example.medical_chatbot.rag.embeddings;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EmbeddingsServiceTest {

    @Test
    void testFakeEmbedding() {
        EmbeddingsService service = new EmbeddingsService();

        float[] vector = service.fakeEmbedding("Bonjour");

        assertNotNull(vector);
        assertEquals(256, vector.length);

        System.out.println("Embedding length = " + vector.length);
    }
}
