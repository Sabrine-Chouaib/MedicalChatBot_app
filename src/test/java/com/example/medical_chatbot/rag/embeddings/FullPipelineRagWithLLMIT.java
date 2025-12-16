package com.example.medical_chatbot.rag.embeddings;

import com.example.medical_chatbot.rag.chunk.MedicalSemanticChunker;
import com.example.medical_chatbot.rag.pdf.PDFTextExtractor;
import com.example.medical_chatbot.rag.prompt.SystemPrompt;
import com.example.medical_chatbot.rag.vectorstore.PineconeClient;
import com.example.medical_chatbot.rag.vectorstore.PineconeStore;
import com.example.medical_chatbot.service.RagServiceLLM;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

/**
 * Test d'intégration (IT) :
 * Pipeline complet RAG + LLM
 *
 * PDF → Extraction → Chunking médical → Embeddings → Pinecone → LLM
 */
public class FullPipelineRagWithLLMIT {

    /* =========================
       🔧 CONFIGURATION GLOBALE
       ========================= */

    private static final String PDF_PATH =
            "C:\\Users\\user\\Documents\\medical-chatbot\\data\\fiche-prevention-clinique-04.pdf";

    private static final String PINECONE_API_KEY =
            "pcsk_2dpnua_DzrGwtVy5ScmuRB2ZKWyKEXnvJQq835YxCzvGvws7uKJVFT2V7BFqX3fjM8L7io";

    private static final String PINECONE_HOST =
            "https://medical-chatbot-ollama-ndr6ggc.svc.aped-4627-b74a.pinecone.io";

    private static final int TOP_K = 3;

    private static final String QUESTION =
            "Quels sont les points clés de la fiche de prévention clinique ?";


    /* =========================
       🧪 TEST PIPELINE COMPLET
       ========================= */

    @Test
    public void testFullPipelineRagWithLLM() throws Exception {

        System.out.println("==== DÉBUT PIPELINE COMPLET RAG + LLM ====");
        MedicalEmbeddingsPipeline.setOllamaBaseUrl("http://localhost:11434/api/embeddings");

        /* 1️⃣ Extraction du texte PDF */
        PDFTextExtractor extractor = new PDFTextExtractor();
        String extractedText = extractor.extractText(PDF_PATH);

        System.out.println("✅ Extraction PDF terminée");
        System.out.println("   → Taille du texte : " + extractedText.length());


        /* 2️⃣ Chunking sémantique médical */
        MedicalSemanticChunker chunker = new MedicalSemanticChunker();
        List<String> chunks = chunker.chunkMedicalText(extractedText);

        System.out.println("✅ Chunking médical terminé");
        System.out.println("   → Nombre de chunks : " + chunks.size());


        /* 3️⃣ Génération des embeddings (Ollama) */
        List<Map<String, Object>> embeddings =
                MedicalEmbeddingsPipeline.generateEmbeddings(chunks, PDF_PATH);

        System.out.println("✅ Embeddings générés");
        System.out.println("   → Nombre de vecteurs : " + embeddings.size());


        /* 4️⃣ Upsert des embeddings dans Pinecone */
        PineconeClient pineconeClient =
                new PineconeClient(PINECONE_API_KEY, PINECONE_HOST);

        pineconeClient.upsertEmbeddings(embeddings);

        System.out.println("✅ Upsert Pinecone réussi");


        /* 5️⃣ Initialisation du service RAG + LLM */
        PineconeStore pineconeStore =
                new PineconeStore(PINECONE_API_KEY, PINECONE_HOST);

        SystemPrompt systemPrompt = new SystemPrompt();

        RagServiceLLM ragServiceLLM =
                new RagServiceLLM(pineconeStore, systemPrompt);


        /* 6️⃣ Génération de la réponse avec le LLM */
        String answer =
                ragServiceLLM.generateAnswerWithLLM(QUESTION, TOP_K);

        // 🔒 Vérification de sécurité
        if (answer == null || answer.isBlank()) {
            throw new IllegalStateException("❌ Réponse LLM vide !");
        }

        System.out.println("✅ Réponse LLM générée :");
        System.out.println("----------------------------------");
        System.out.println(answer);
        System.out.println("----------------------------------");

        System.out.println("==== FIN PIPELINE COMPLET RAG + LLM ====");
    }
}
