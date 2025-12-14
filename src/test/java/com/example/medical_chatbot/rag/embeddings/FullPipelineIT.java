package com.example.medical_chatbot.rag.embeddings;

import com.example.medical_chatbot.rag.chunk.MedicalSemanticChunker;
import com.example.medical_chatbot.rag.pdf.PDFTextExtractor;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class FullPipelineIT {

    @Test
    void testFullPipeline() throws Exception {
        // ✅ Utiliser endpoint fixe
        MedicalEmbeddingsPipeline.setOllamaBaseUrl("http://localhost:11434/api/embeddings");

        String pdfPath = Paths.get(
                "C:\\Users\\user\\Documents\\medical-chatbot\\data\\fiche-prevention-clinique-04.pdf"
        ).toString();

        PDFTextExtractor extractor = new PDFTextExtractor();
        String text = extractor.extractText(pdfPath);

        MedicalSemanticChunker chunker = new MedicalSemanticChunker();
        List<String> chunks = chunker.chunkMedicalText(text);

        List<Map<String, Object>> embeddings = MedicalEmbeddingsPipeline.generateEmbeddings(chunks, "fiche-prevention-clinique-04");

        System.out.println("✅ Embeddings générés : " + embeddings.size());
        System.out.println("Exemple d'un embedding : " + embeddings.get(0));
    }
}
