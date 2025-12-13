package com.sabrine.medicalchatbot.service;

import com.sabrine.medicalchatbot.Entity.Chunk;
import com.sabrine.medicalchatbot.repository.ChunkRepository;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IngestionService {

    private final ChunkRepository chunkRepository;
    private final EmbeddingService embeddingService;

    public void ingestPdf(File pdfFile) throws Exception {
        String text = extractText(pdfFile);
        List<String> chunks = splitIntoChunks(text);

        for (String c : chunks) {
            float[] embedding = embeddingService.embed(c);

            Chunk chunk = new Chunk();
            chunk.setContent(c);
            chunk.setEmbedding(embedding);

            chunkRepository.save(chunk);
        }
    }

    private String extractText(File file) throws IOException {
        PDDocument doc = PDDocument.load(file);
        PDFTextStripper stripper = new PDFTextStripper();
        return stripper.getText(doc);
    }

    private List<String> splitIntoChunks(String text) {
        int chunkSize = 800;
        List<String> chunks = new ArrayList<>();

        for (int i = 0; i < text.length(); i += chunkSize) {
            chunks.add(text.substring(i, Math.min(text.length(), i + chunkSize)));
        }
        return chunks;
    }
}