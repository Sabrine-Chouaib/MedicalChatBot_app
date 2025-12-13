package com.sabrine.medicalchatbot.controller;
import com.sabrine.medicalchatbot.service.IngestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;


@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final IngestionService ingestionService;

    @PostMapping("/ingest")
    public String ingest(@RequestParam("file") MultipartFile file) throws Exception {
        File temp = File.createTempFile("pdf", ".pdf");
        file.transferTo(temp);
        ingestionService.ingestPdf(temp);
        return "PDF ingested successfully";
    }
}
