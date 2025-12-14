package com.example.medical_chatbot.rag;

import com.example.medical_chatbot.rag.chunk.MedicalSemanticChunker;
import com.example.medical_chatbot.rag.pdf.PDFTextExtractor;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MedicalSemanticChunkerIT {

    @Test
    void shouldChunkMedicalTextSemantically() throws Exception {

        PDFTextExtractor extractor = new PDFTextExtractor();
        MedicalSemanticChunker chunker = new MedicalSemanticChunker();

        String pdfPath = Paths.get(
                "C:\\Users\\user\\Documents\\medical-chatbot\\data\\fiche-prevention-clinique-04.pdf"
        ).toString();

        // 1️⃣ Extraction
        String extractedText = extractor.extractText(pdfPath);
        assertNotNull(extractedText);
        assertFalse(extractedText.isBlank());

        // 2️⃣ Chunking sémantique
        List<String> chunks = chunker.chunkMedicalText(extractedText);

        assertNotNull(chunks);
        assertTrue(chunks.size() > 5);

        // 3️⃣ OBSERVATION
        System.out.println("===== CHUNKS MÉDICAUX =====");
        int i = 1;
        for (String chunk : chunks) {
            System.out.println("\n--- CHUNK " + i++ + " ---");
            System.out.println(chunk);
        }
        System.out.println("==========================");

      for (String chunk : chunks) {
    assertNotNull(chunk);
    assertFalse(chunk.isBlank());
}


    }
}
