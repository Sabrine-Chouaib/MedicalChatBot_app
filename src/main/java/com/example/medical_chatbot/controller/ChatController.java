package com.example.medical_chatbot.controller;

import com.example.medical_chatbot.model.QAResponse;
import com.example.medical_chatbot.service.RagServiceLLM;
import io.opentelemetry.context.Scope;
import org.springframework.web.bind.annotation.*;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final RagServiceLLM ragServiceLLM;
    private final Tracer tracer;

    // ✅ Injection via Spring
    public ChatController(RagServiceLLM ragServiceLLM, Tracer tracer) {
        this.ragServiceLLM = ragServiceLLM;
        this.tracer = tracer;
    }

    /**
     * POST /api/chat
     * Body JSON :
     * {
     *   "question": "Quel est le traitement de l'hypertension ?",
     *   "k": 3
     * }
     */
    @PostMapping
    public QAResponse chat(@RequestBody ChatRequest request) {
        Span pipeline = tracer.spanBuilder("RAG Pipeline").startSpan();
        try (Scope scope = pipeline.makeCurrent()) {

            // 1️⃣ PDF Extraction
            Span pdfSpan = tracer.spanBuilder("PDF Extraction").startSpan();
            String text;
            try {
                text = ragServiceLLM.extractPdfText(request);
                pdfSpan.setAttribute("pdf.text.length", text != null ? text.length() : 0);
            } finally {
                pdfSpan.end();
            }

            // 2️⃣ Chunking
            Span chunkSpan = tracer.spanBuilder("Medical Chunking").startSpan();
            List<String> chunks;
            try {
                chunks = ragServiceLLM.chunkText(text);
                chunkSpan.setAttribute("chunk.count", chunks.size());
            } finally {
                chunkSpan.end();
            }

            // 3️⃣ Embeddings
            Span embedSpan = tracer.spanBuilder("Ollama Embedding").startSpan();
            List<double[]> vectors;
            try {
                vectors = ragServiceLLM.embedChunks(chunks, "data/fiche-prevention-clinique-04.pdf");
                embedSpan.setAttribute("embedding.count", vectors.size());
            } finally {
                embedSpan.end();
            }

            // 4️⃣ Pinecone Query
            Span pineconeSpan = tracer.spanBuilder("Pinecone Query").startSpan();
            JsonNode matches;
            try {
                matches = ragServiceLLM.queryPinecone(vectors, request.getK());
                int count = matches.has("matches") && matches.get("matches").isArray()
                        ? matches.get("matches").size() : 0;
                pineconeSpan.setAttribute("pinecone.matches.count", count);
            } finally {
                pineconeSpan.end();
            }

            // 5️⃣ LLM Generation
            Span llmSpan = tracer.spanBuilder("Ollama Generation").startSpan();
            String answer;
            try {
                answer = ragServiceLLM.generateAnswerWithLLM(request.getQuestion(), request.getK());
                llmSpan.setAttribute("llm.answer.length", answer != null ? answer.length() : 0);
            } finally {
                llmSpan.end();
            }

            return new QAResponse(answer);

        } catch (Exception e) {
            pipeline.recordException(e);
            return new QAResponse("❌ Erreur lors de la génération : " + e.getMessage());
        } finally {
            pipeline.end();
        }
    }
}
