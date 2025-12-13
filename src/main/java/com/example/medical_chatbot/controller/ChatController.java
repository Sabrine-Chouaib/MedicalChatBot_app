package com.example.medical_chatbot.controller;

import com.example.medical_chatbot.model.QAResponse;
import com.example.medical_chatbot.service.RagService;
import org.springframework.web.bind.annotation.*;

import java.io.File;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final RagService ragService;

    public ChatController(RagService ragService) {
        this.ragService = ragService;
    }

    /**
     * Endpoint pour indexer tous les PDFs dans un dossier donné.
     * Chaque PDF est découpé en chunks grâce à PDFChunker + TextChunker,
     * puis les embeddings sont stockés dans Pinecone.
     *
     * Exemple : POST /api/store-index?dataFolder=data/
     */
    @PostMapping("/store-index")
    public String storeIndex(@RequestParam(value = "dataFolder", defaultValue = "data/") String dataFolder) {
        File folder = new File(dataFolder);
        if (!folder.exists() || !folder.isDirectory()) {
            return "❌ Dossier non trouvé : " + dataFolder;
        }

        File[] pdfFiles = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".pdf"));
        if (pdfFiles == null || pdfFiles.length == 0) return "❌ Aucun PDF trouvé dans " + dataFolder;

        try {
            for (File pdf : pdfFiles) {
                System.out.println("📄 Indexation du PDF : " + pdf.getName());
                ragService.indexPDF(pdf.getAbsolutePath());
            }
            return "✅ Tous les PDFs indexés avec succès (" + pdfFiles.length + " fichiers)";
        } catch (Exception e) {
            e.printStackTrace();
            return "❌ Erreur lors de l'indexation : " + e.getMessage();
        }
    }

    /**
     * Endpoint de chat : permet de poser une question au système RAG.
     * Le service RAG recherche dans Pinecone et génère une réponse avec Ollama.
     *
     * Exemple : POST /api/chat?question=Quel est le traitement pour X ?&k=3
     */
    @PostMapping("/chat")
    public QAResponse chat(@RequestParam("question") String question,
                           @RequestParam(value = "k", defaultValue = "3") int topK) {
        try {
            // Génère la réponse en utilisant la recherche + Ollama LLM
            String answer = ragService.generateAnswer(question, topK);
            return new QAResponse(answer);
        } catch (Exception e) {
            e.printStackTrace();
            return new QAResponse("❌ Erreur lors de la génération de la réponse : " + e.getMessage());
        }
    }
}
