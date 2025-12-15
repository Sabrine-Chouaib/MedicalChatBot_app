package com.example.medical_chatbot.controller;

import com.example.medical_chatbot.model.QAResponse;
import com.example.medical_chatbot.service.RagServiceLLM;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final RagServiceLLM ragServiceLLM;

    // ✅ Injection propre Spring
    public ChatController(RagServiceLLM ragServiceLLM) {
        this.ragServiceLLM = ragServiceLLM;
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

        try {
            String answer = ragServiceLLM.generateAnswerWithLLM(
                    request.getQuestion(),
                    request.getK()
            );

            return new QAResponse(answer);

        } catch (Exception e) {
            e.printStackTrace();
            return new QAResponse(
                    "❌ Erreur lors de la génération : " + e.getMessage()
            );
        }
    }
}
