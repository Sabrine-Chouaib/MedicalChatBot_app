package com.example.medical_chatbot.controller;

import com.example.medical_chatbot.model.QAResponse;
import com.example.medical_chatbot.rag.prompt.SystemPrompt;
import com.example.medical_chatbot.rag.vectorstore.PineconeStore;
import com.example.medical_chatbot.service.RagServiceLLM;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final RagServiceLLM ragServiceLLM;

    public ChatController() {
        // ⚡ Initialisation minimale pour tester la pipeline
        String pineconeApiKey = "pcsk_2dpnua_DzrGwtVy5ScmuRB2ZKWyKEXnvJQq835YxCzvGvws7uKJVFT2V7BFqX3fjM8L7io";
        String pineconeHost = "https://medical-chatbot-ollama-ndr6ggc.svc.aped-4627-b74a.pinecone.io";

        PineconeStore pineconeStore = new PineconeStore(pineconeApiKey, pineconeHost);
        SystemPrompt systemPrompt = new SystemPrompt();

        this.ragServiceLLM = new RagServiceLLM(pineconeStore, systemPrompt);
    }

    /**
     * Endpoint de chat : permet de poser une question au système RAG + LLM.
     * Exemple : POST /api/chat?question=Quel est le traitement pour X ?&k=3
     */
    @PostMapping("/chat")
    public QAResponse chat(@RequestParam("question") String question,
                           @RequestParam(value = "k", defaultValue = "3") int topK) {
        try {
            String answer = ragServiceLLM.generateAnswerWithLLM(question, topK);
            return new QAResponse(answer);
        } catch (Exception e) {
            e.printStackTrace();
            return new QAResponse("❌ Erreur lors de la génération de la réponse : " + e.getMessage());
        }
    }
}
