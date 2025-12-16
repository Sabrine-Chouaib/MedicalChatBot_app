package com.example.medical_chatbot.config;

import com.example.medical_chatbot.rag.embeddings.MedicalEmbeddingsPipeline;
import com.example.medical_chatbot.rag.prompt.SystemPrompt;
import com.example.medical_chatbot.rag.vectorstore.PineconeStore;
import com.example.medical_chatbot.service.RagServiceLLM;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RagConfig {

    @Value("${pinecone.api.key}")
    private String pineconeApiKey;

    @Value("${pinecone.host}")
    private String pineconeHost;

    @Value("${ollama.base-url}")
    private String ollamaBaseUrl;

    @Value("${ollama.embeddings-path}")
    private String embeddingsPath;

    @Bean
    public PineconeStore pineconeStore() {
        return new PineconeStore(
                pineconeApiKey,
                pineconeHost
        );
    }

    @Bean
    public SystemPrompt systemPrompt() {
        return new SystemPrompt();
    }

    @Bean
    public RagServiceLLM ragServiceLLM(
            PineconeStore pineconeStore,
            SystemPrompt systemPrompt
    ) {
         // ✅ URL COMPLÈTE pour embeddings
        MedicalEmbeddingsPipeline.setOllamaBaseUrl(
                ollamaBaseUrl + embeddingsPath
        );
        // 🔥 Injecter aussi l’URL Ollama si besoin plus tard
        return new RagServiceLLM(pineconeStore, systemPrompt);
    }
}
