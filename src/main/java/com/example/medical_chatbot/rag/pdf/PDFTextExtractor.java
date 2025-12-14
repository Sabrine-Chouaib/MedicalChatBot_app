package com.example.medical_chatbot.rag.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;

import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;

/**
 * Extraction PDF HAUTE QUALITÉ pour RAG médical
 */
public class PDFTextExtractor {

    public String extractText(String pdfPath) throws IOException {

        File pdfFile = new File(pdfPath);
        if (!pdfFile.exists()) {
            throw new IllegalArgumentException("PDF introuvable : " + pdfPath);
        }

        try (PDDocument document = PDDocument.load(pdfFile)) {

            String globalText = extractGlobalText(document);
            String areaText = extractByAreaText(document);

            return normalizeText(globalText + "\n\n" + areaText);
        }
    }

    /**
     * 1️⃣ Extraction globale (flow naturel)
     */
    private String extractGlobalText(PDDocument document) throws IOException {

        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setStartPage(1);
        stripper.setEndPage(document.getNumberOfPages());

        stripper.setSortByPosition(true);
        stripper.setLineSeparator("\n");
        stripper.setParagraphStart("\n\n");
        stripper.setParagraphEnd("\n\n");

        return stripper.getText(document);
    }

    /**
     * 2️⃣ Extraction par zones (colonnes / tableaux)
     */
    private String extractByAreaText(PDDocument document) throws IOException {

    StringBuilder sb = new StringBuilder();

    for (PDPage page : document.getPages()) {

        PDFTextStripperByArea stripper = new PDFTextStripperByArea();
        stripper.setSortByPosition(true);

        PDRectangle mediaBox = page.getMediaBox();
        float width = mediaBox.getWidth();
        float height = mediaBox.getHeight();

        Rectangle leftColumn = new Rectangle(
                0, 0, (int) (width / 2), (int) height
        );

        Rectangle rightColumn = new Rectangle(
                (int) (width / 2), 0, (int) (width / 2), (int) height
        );

        stripper.addRegion("left", leftColumn);
        stripper.addRegion("right", rightColumn);

        stripper.extractRegions(page);

        sb.append(stripper.getTextForRegion("left")).append("\n");
        sb.append(stripper.getTextForRegion("right")).append("\n");
    }

    return sb.toString();
}


    /**
     * 3️⃣ Nettoyage minimal mais RAG-friendly
     */
    private String normalizeText(String text) {
        return text
                .replace("\uFFFD", "'")
                .replaceAll("[ ]{2,}", " ")
                .replaceAll("\n{3,}", "\n\n")
                .trim();
    }
}
