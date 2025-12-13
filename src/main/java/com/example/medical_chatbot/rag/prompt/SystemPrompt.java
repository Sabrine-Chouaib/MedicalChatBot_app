package com.example.medical_chatbot.rag.prompt;



import org.springframework.stereotype.Component;

/**
 * System prompt utilisé dans la génération RAG.
 */
@Component
public class SystemPrompt {

    public String buildPrompt(String question, String retrievedContext) {
        String system = "You are a Medical assistant for question-answering tasks. "
                + "Use the following pieces of retrieved context to answer the question. "
                + "If you don't know the answer, say that you don't know. Use three sentences maximum and keep the answer concise.\n\n";

        String contextBlock = "Context:\n" + retrievedContext + "\n\n";
        String userBlock = "Question: " + question + "\nAnswer:";

        return system + contextBlock + userBlock;
    }
}

