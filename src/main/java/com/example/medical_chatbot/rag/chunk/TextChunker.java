package com.example.medical_chatbot.rag.chunk;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.List; // ✅ Important : ajouter cet import
import java.util.ArrayList; // ✅ ArrayList

@Component
public class TextChunker {

    @Value("${rag.chunk.size:200}")
    private int chunkSize;

    @Value("${rag.chunk.overlap:20}")
    private int overlap;

    /**
     * Méthode traditionnelle qui retourne tous les chunks en mémoire
     */
    public List<String> chunk(String text) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isBlank()) return chunks;

        int start = 0;
        int length = text.length();

        while (start < length) {
            int end = Math.min(length, start + chunkSize);
            chunks.add(text.substring(start, end));

            start = end - overlap;
            if (start < 0) start = 0;
        }
        return chunks;
    }

    /**
     * Streaming / chunking itératif
     * Retourne un Iterable pour éviter de stocker tous les chunks en mémoire
     */
    public Iterable<String> streamChunks(String text) {
        return () -> new Iterator<>() {
            private int start = 0;

            @Override
            public boolean hasNext() {
                return text != null && start < text.length();
            }

            @Override
            public String next() {
                if (!hasNext()) throw new NoSuchElementException();
                int end = Math.min(text.length(), start + chunkSize);
                String chunk = text.substring(start, end);
                start = end - overlap;
                if (start < 0) start = 0;
                return chunk;
            }
        };
    }

    /**
     * Getters
     */
    public int getChunkSize() {
        return chunkSize;
    }

    public int getOverlap() {
        return overlap;
    }
}
