package com.sabrine.medicalchatbot.service;

import com.sabrine.medicalchatbot.Entity.Chunk;
import com.sabrine.medicalchatbot.repository.ChunkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RAGService {

    private final EmbeddingService embeddingService;
    private final ChunkRepository chunkRepository;
    private final LLMService llmService;

    public String answer(String question) {

        // (1) Embedding de la question
        float[] qVector = embeddingService.embed(question);

        // (2) Récupération top-k chunks
        List<Chunk> retrieved = chunkRepository.searchByEmbedding(qVector, 5);

        // (3) Construire le contexte
        StringBuilder context = new StringBuilder();
        for (Chunk c : retrieved) context.append(c.getContent()).append("\n");

        // (4) Prompt final
        String prompt = """
                You are a medical assistant.
                Use the following context to answer the user question.
                If unsure, say "I don't know".

                Context:
                %s

                Question: %s
                """.formatted(context, question);

        // (5) Appel LLM via Ollama
        return llmService.generate(prompt);
    }
}
