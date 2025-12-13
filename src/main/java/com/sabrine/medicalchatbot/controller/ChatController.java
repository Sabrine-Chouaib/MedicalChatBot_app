package com.sabrine.medicalchatbot.controller;

import com.sabrine.medicalchatbot.dto.ChatRequest;
import com.sabrine.medicalchatbot.dto.ChatResponse;
import com.sabrine.medicalchatbot.service.RAGService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final RAGService ragService;

    @PostMapping
    public ChatResponse chat(@RequestBody ChatRequest request) {
        String answer = ragService.answer(request.getQuestion());
        return new ChatResponse(answer);
    }
}
