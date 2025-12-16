package com.example.medical_chatbot.rag.chunk;

import io.opentelemetry.instrumentation.annotations.WithSpan;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Chunking sémantique médical amélioré
 * - Sections cliniques
 * - Préserve contexte
 * - Taille safe pour embeddings Ollama
 */
public class MedicalSemanticChunker {

    // Taille max safe pour embeddings
    private static final int MAX_CHUNK_LENGTH = 500; // ⚠️ plus petit que 1200 pour Ollama nomic

    /**
     * Méthode principale
     */
    @WithSpan("Chunking médical")
    public List<String> chunkMedicalText(String extractedText) {
        List<String> chunks = new ArrayList<>();
        if (extractedText == null || extractedText.isBlank()) return chunks;

        // Découper par sections médicales
        List<MedicalSection> sections = splitIntoSections(extractedText);

        // Transformer chaque section en chunks concis
        for (MedicalSection section : sections) {
            chunks.addAll(splitSectionSafely(section));
        }

        return chunks;
    }

    /**
     * Découpe par titres cliniques
     */
    private List<MedicalSection> splitIntoSections(String text) {
        List<MedicalSection> sections = new ArrayList<>();
        Pattern titlePattern = Pattern.compile("(?m)^[A-ZÉÈÀÙÂÊÎÔÛÇ\\s]{4,}$");
        Matcher matcher = titlePattern.matcher(text);

        int lastIndex = 0;
        String lastTitle = "CONTEXTE CLINIQUE";

        while (matcher.find()) {
            int start = matcher.start();
            if (start > lastIndex) {
                String content = text.substring(lastIndex, start).trim();
                sections.add(new MedicalSection(lastTitle, content));
            }
            lastTitle = matcher.group().trim();
            lastIndex = matcher.end();
        }

        if (lastIndex < text.length()) {
            sections.add(new MedicalSection(lastTitle, text.substring(lastIndex).trim()));
        }

        return sections;
    }

    /**
     * Découpe une section trop longue en chunks sûrs
     */
    private List<String> splitSectionSafely(MedicalSection section) {
        List<String> chunks = new ArrayList<>();
        String[] paragraphs = section.content.split("\n\n");
        StringBuilder currentChunk = new StringBuilder(section.title + "\n");

        for (String paragraph : paragraphs) {
            // Si le paragraphe seul est trop long, on le coupe
            if (paragraph.length() > MAX_CHUNK_LENGTH) {
                for (int i = 0; i < paragraph.length(); i += MAX_CHUNK_LENGTH) {
                    int end = Math.min(paragraph.length(), i + MAX_CHUNK_LENGTH);
                    String chunkText = paragraph.substring(i, end).trim();
                    chunks.add(section.title + "\n" + chunkText);
                }
                continue;
            }

            // Ajouter au chunk courant si safe
            if (currentChunk.length() + paragraph.length() > MAX_CHUNK_LENGTH) {
                chunks.add(currentChunk.toString().trim());
                currentChunk = new StringBuilder(section.title + "\n");
            }

            currentChunk.append(paragraph).append("\n\n");
        }

        if (!currentChunk.isEmpty()) {
            chunks.add(currentChunk.toString().trim());
        }

        return chunks;
    }

    /**
     * Structure interne
     */
    public static class MedicalSection {
        public String title;
        public String content;
        public MedicalSection(String title, String content) {
            this.title = title;
            this.content = content;
        }
    }
}
