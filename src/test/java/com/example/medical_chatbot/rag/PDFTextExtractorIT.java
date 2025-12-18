package com.example.medical_chatbot.rag;

import com.example.medical_chatbot.rag.pdf.PDFTextExtractor;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class PDFTextExtractorIT {

    @Test
    void shouldExtractRichTextFromPdf() throws Exception {

        PDFTextExtractor extractor = new PDFTextExtractor();

        String pdfPath = Paths.get(
                "C:\\Users\\sabre\\OneDrive\\bureau\\projet-final\\MedicalChatBot_app\\data\\fiche-prevention-clinique-04.pdf"
        ).toString();

        String text = extractor.extractText(pdfPath);

        assertNotNull(text);
        assertFalse(text.isBlank());
        assertTrue(text.length() > 2000, "Texte trop court → extraction incomplète");

        System.out.println("===== EXTRACTION PDF HAUTE QUALITÉ =====");
        System.out.println(text.substring(0, Math.min(3000, text.length())));
        System.out.println("=======================================");
    }
}
