package com.example.medical_chatbot.controller;

public class ChatRequest {

    private String question;
    private int k = 3;

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public int getK() {
        return k;
    }

    public void setK(int k) {
        this.k = k;
    }
}
