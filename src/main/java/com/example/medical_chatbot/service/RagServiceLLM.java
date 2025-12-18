package com.example.medical_chatbot.service;

import com.example.medical_chatbot.rag.chunk.MedicalSemanticChunker;
import com.example.medical_chatbot.rag.chunk.TextChunker;
import com.example.medical_chatbot.rag.embeddings.MedicalEmbeddingsPipeline;
import com.example.medical_chatbot.rag.prompt.SystemPrompt;
import com.example.medical_chatbot.rag.vectorstore.PineconeStore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.medical_chatbot.controller.ChatRequest;
import com.example.medical_chatbot.rag.pdf.PDFTextExtractor;
import com.example.medical_chatbot.rag.pdf.PDFChunker;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import org.springframework.stereotype.Service;

import java.util.List;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.io.IOException;
import java.util.ArrayList;

@Service
public class RagServiceLLM {

    private final PineconeStore pineconeStore;
    private final SystemPrompt systemPrompt;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Tracer tracer;
    private final PDFTextExtractor pdfTextExtractor;
    private final PDFChunker pdfChunker;
    private final MedicalSemanticChunker medicalSemanticChunker;
    private final TextChunker textChunker;

    public RagServiceLLM(Tracer tracer,
                         PDFTextExtractor pdfTextExtractor,
                         PDFChunker pdfChunker,
                         PineconeStore pineconeStore,
                         SystemPrompt systemPrompt,
                         MedicalSemanticChunker medicalSemanticChunker,
                         TextChunker textChunker) {
        this.tracer = tracer;
        this.pdfTextExtractor = pdfTextExtractor;
        this.pdfChunker = pdfChunker;
        this.pineconeStore = pineconeStore;
        this.systemPrompt = systemPrompt;
        this.medicalSemanticChunker = medicalSemanticChunker;
        this.textChunker = textChunker;
    }

    public String extractPdfText(ChatRequest request) throws Exception {
        Span span = tracer.spanBuilder("PDFTextExtractor.extractText").startSpan();
        try {
            String pdfPath = "data/fiche-prevention-clinique-04.pdf";
            String text = pdfTextExtractor.extractText(pdfPath);
            span.setAttribute("pdf.text.length", text.length());
            return text;
        } finally {
            span.end();
        }
    }

    public List<String> extractPdfChunks(ChatRequest request) throws Exception {
        Span span = tracer.spanBuilder("PDFChunker.extractTextChunks").startSpan();
        try {
            String pdfPath = "src/main/resources/data/fiche_prevention.pdf";
            List<String> chunks = pdfChunker.extractTextChunks(pdfPath);
            span.setAttribute("pdf.chunks.count", chunks.size());
            return chunks;
        } finally {
            span.end();
        }
    }

    public List<String> chunkText(String text) {
        Span span = tracer.spanBuilder("MedicalSemanticChunker.chunkMedicalText").startSpan();
        try {
            List<String> chunks = medicalSemanticChunker.chunkMedicalText(text);
            span.setAttribute("chunk.count", chunks.size());
            span.setAttribute("chunk.avg.length",
                    chunks.stream().mapToInt(String::length).average().orElse(0));
            return chunks;
        } finally {
            span.end();
        }
    }

    public List<double[]> embedChunks(List<String> chunks, String source) throws IOException {
        Span span = tracer.spanBuilder("MedicalEmbeddingsPipeline.generateEmbeddings").startSpan();
        try {
            List<Map<String, Object>> rawEmbeddings = MedicalEmbeddingsPipeline.generateEmbeddings(chunks, source);
            span.setAttribute("embedding.count", rawEmbeddings.size());

            List<double[]> vectors = new ArrayList<>();
            for (Map<String, Object> entry : rawEmbeddings) {
                Object valuesObj = entry.get("values");
                if (valuesObj instanceof List) {
                    List<?> valuesList = (List<?>) valuesObj;
                    span.setAttribute("embedding.dim", valuesList.size());
                    double[] vector = new double[valuesList.size()];
                    for (int i = 0; i < valuesList.size(); i++) {
                        vector[i] = ((Number) valuesList.get(i)).doubleValue();
                    }
                    vectors.add(vector);
                }
            }
            return vectors;
        } finally {
            span.end();
        }
    }

    public JsonNode queryPinecone(List<double[]> vectors, int topK) throws IOException {
        Span span = tracer.spanBuilder("PineconeStore.query").startSpan();
        try {
            double[] vector = vectors.isEmpty() ? new double[0] : vectors.get(0);
            JsonNode matches = pineconeStore.query(vector, topK);

            span.setAttribute("pinecone.topK", topK);
            int count = matches.has("matches") && matches.get("matches").isArray()
                    ? matches.get("matches").size() : 0;
            span.setAttribute("pinecone.matches.count", count);

            return matches;
        } finally {
            span.end();
        }
    }

    public String generateAnswerWithLLM(String question, int topK) throws Exception {
        Span span = tracer.spanBuilder("LLM.generateAnswer").startSpan();
        try {
            List<Map<String, Object>> embeddings =
                    MedicalEmbeddingsPipeline.generateEmbeddings(List.of(question), "question");

            @SuppressWarnings("unchecked")
            double[] queryVector = ((List<Double>) embeddings.get(0).get("values"))
                    .stream().mapToDouble(Double::doubleValue).toArray();

            JsonNode searchResult = pineconeStore.query(queryVector, topK);

            StringBuilder retrievedContext = new StringBuilder();
            if (searchResult != null && searchResult.has("matches")) {
                for (JsonNode match : searchResult.get("matches")) {
                    JsonNode metadata = match.get("metadata");
                    if (metadata == null) continue;
                    JsonNode contentNode = metadata.get("content");
                    if (contentNode == null || contentNode.asText().isBlank()) continue;
                    retrievedContext.append(contentNode.asText()).append("\n");
                }
            }

            String prompt = systemPrompt.buildPrompt(question, retrievedContext.toString());
            span.setAttribute("llm.prompt.length", prompt.length());
            span.setAttribute("llm.context.length", retrievedContext.length());
            span.setAttribute("llm.question", question);

            Map<String, Object> payload = new HashMap<>();
            payload.put("model", "tinyllama:latest");
            payload.put("prompt", prompt);
            payload.put("stream", false);

            String jsonPayload = objectMapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:11434/api/generate"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            JsonNode responseJson = objectMapper.readTree(response.body());
            JsonNode responseNode = responseJson.path("response");

            if (responseNode.isMissingNode() || responseNode.asText().isBlank()) {
                throw new IllegalStateException("Réponse Ollama invalide : " + responseJson.toPrettyString());
            }

            String finalAnswer = responseNode.asText().trim();
            span.setAttribute("llm.response.length", finalAnswer.length());
            return finalAnswer;
        } finally {
            span.end();
        }
    }
}
