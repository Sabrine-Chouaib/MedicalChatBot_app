package com.example.medical_chatbot.model;

public class QAResponse {
    private String answer;

    public QAResponse() {}

    public QAResponse(String answer) { this.answer = answer; }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
}