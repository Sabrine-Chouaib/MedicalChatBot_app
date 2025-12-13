package com.example.medical_chatbot.rag.pdf;

import com.example.medical_chatbot.rag.chunk.TextChunker;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Component
public class PDFChunker {

    private final TextChunker textChunker;

    public PDFChunker(TextChunker textChunker) {
        this.textChunker = textChunker;
    }

    /**
     * Streaming sécurisé : ferme automatiquement le PDF après extraction
     */
    public Iterable<String> extractTextChunksStream(String pdfPath) throws IOException {
        File pdfFile = new File(pdfPath);
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setStartPage(1);
            stripper.setEndPage(document.getNumberOfPages());
            String text = stripper.getText(document);
            return textChunker.streamChunks(text);
        }
    }

    /**
     * Méthode classique
     */
    public List<String> extractTextChunks(String pdfPath) throws IOException {
        File pdfFile = new File(pdfPath);
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setStartPage(1);
            stripper.setEndPage(document.getNumberOfPages());
            String text = stripper.getText(document);
            return textChunker.chunk(text);
        }
    }
}
