package com.example.medical_chatbot.rag;

import com.example.medical_chatbot.rag.embeddings.EmbeddingsService;
import com.example.medical_chatbot.rag.pdf.PDFChunker;
import com.example.medical_chatbot.rag.vectorstore.PineconeStore;
import com.example.medical_chatbot.rag.prompt.SystemPrompt;
import com.example.medical_chatbot.rag.chunk.TextChunker;
import com.example.medical_chatbot.service.RagService;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class RagServiceIT {

    @Test
void shouldIndexPDFAndGenerateAnswer() throws Exception {
    // 1️⃣ EmbeddingsService
    EmbeddingsService embeddingsService = new EmbeddingsService();
    embeddingsService.setOllamaBaseUrl("http://localhost:11434");

    // 2️⃣ PineconeStore réel
    PineconeStore pineconeStore = new PineconeStore(HttpClient.newHttpClient());
    pineconeStore.setApiKey("TA_VRAIE_CLE_PINECONE");
    pineconeStore.setHost("https://medical-chatbot-ollama-ndr6ggc.svc.aped-4627-b74a.pinecone.io");

    // 3️⃣ SystemPrompt
    SystemPrompt systemPrompt = new SystemPrompt();

    // 4️⃣ TextChunker et PDFChunker
    TextChunker textChunker = new TextChunker();
    PDFChunker pdfChunker = new PDFChunker(textChunker);

    // 5️⃣ RagService
    RagService ragService = new RagService(embeddingsService, pineconeStore, systemPrompt, pdfChunker);

    // 6️⃣ Indexation PDF streaming sécurisé
    String pdfPath = Paths.get("C:\\Users\\user\\Documents\\medical-chatbot\\data\\fiche-prevention-clinique-04.pdf").toString();
    ragService.indexPDF(pdfPath); // PDF fermé automatiquement après extraction

    // 7️⃣ Test recherche Pinecone
    JsonNode result = ragService.search("À quel âge faut-il commencer la prévention clinique ?", 3);
    assertNotNull(result);
    assertTrue(result.has("matches"));
    assertTrue(result.get("matches").size() > 0);

    // 8️⃣ Génération réponse Ollama
    String answer = ragService.generateAnswer("À quel âge faut-il commencer la prévention clinique ?", 3);
    System.out.println("💬 Réponse LLM : " + answer);
    assertNotNull(answer);
    assertTrue(answer.length() > 0);
}

}
