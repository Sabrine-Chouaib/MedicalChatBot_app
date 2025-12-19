package com.example.medical_chatbot.config;

import com.example.medical_chatbot.rag.chunk.MedicalSemanticChunker;
import com.example.medical_chatbot.rag.chunk.TextChunker;
import com.example.medical_chatbot.rag.embeddings.MedicalEmbeddingsPipeline;
import com.example.medical_chatbot.rag.pdf.PDFChunker;
import com.example.medical_chatbot.rag.pdf.PDFTextExtractor;
import com.example.medical_chatbot.rag.prompt.SystemPrompt;
import com.example.medical_chatbot.rag.vectorstore.PineconeStore;
import com.example.medical_chatbot.service.RagServiceLLM;
import io.opentelemetry.api.trace.Tracer;
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

    // ✅ Bean Pinecone
    @Bean
    public PineconeStore pineconeStore() {
        return new PineconeStore(pineconeApiKey, pineconeHost);
    }

    // ✅ Bean SystemPrompt
    @Bean
    public SystemPrompt systemPrompt() {
        return new SystemPrompt();
    }

    // ✅ Bean PDFTextExtractor
    @Bean
    public PDFTextExtractor pdfTextExtractor() {
        return new PDFTextExtractor();
    }

    // ✅ Bean PDFChunker
    @Bean
    public PDFChunker pdfChunker(TextChunker textChunker) {
        return new PDFChunker(textChunker);
    }

    // ✅ Bean MedicalSemanticChunker
    @Bean
    public MedicalSemanticChunker medicalSemanticChunker() {
        return new MedicalSemanticChunker();
    }

    // ✅ Bean TextChunker
    @Bean
    public TextChunker textChunker() {
        return new TextChunker();
    }

    // ✅ Bean RagServiceLLM avec tous les paramètres attendus
    @Bean
    public RagServiceLLM ragServiceLLM(
            Tracer tracer,
            PDFTextExtractor pdfTextExtractor,
            PDFChunker pdfChunker,
            PineconeStore pineconeStore,
            SystemPrompt systemPrompt,
            MedicalSemanticChunker medicalSemanticChunker,
            TextChunker textChunker
    ) {
        // URL complète pour embeddings
        MedicalEmbeddingsPipeline.setOllamaBaseUrl(ollamaBaseUrl + embeddingsPath);

        return new RagServiceLLM(
                tracer,
                pdfTextExtractor,
                pdfChunker,
                pineconeStore,
                systemPrompt,
                medicalSemanticChunker,
                textChunker
        );
    }
}
