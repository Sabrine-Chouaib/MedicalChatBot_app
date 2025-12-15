package com.example.medical_chatbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MedicalChatbotApplication {
    public static void main(String[] args) {
        System.out.println("🚀 Démarrage du Medical Chatbot...");

        SpringApplication.run(MedicalChatbotApplication.class, args);
    }
}
