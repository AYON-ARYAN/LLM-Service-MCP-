package com.mcp.ragmcp.controller;

import com.mcp.ragmcp.model.Chunk;
import com.mcp.ragmcp.service.EmbeddingService;
import com.mcp.ragmcp.service.VectorDBService;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/docs")
@CrossOrigin(origins = "*")
public class DocumentController {

    @Autowired
    private EmbeddingService embeddingService;

    @Autowired
    private VectorDBService vectorDB;

    public static String storedText = "";

    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file) {
        try {
            Tika tika = new Tika();
            String text = tika.parseToString(file.getInputStream());
            storedText = text;

            // split into chunks of 500 characters
            // split into chunks of 1000 characters with 200 overlap
            int size = 1000;
            int overlap = 200;
            int count = 0;
            String fileName = file.getOriginalFilename();

            for (int i = 0; i < text.length(); i += (size - overlap)) {
                int end = Math.min(i + size, text.length());
                String chunkText = text.substring(i, end);

                // generate embedding
                List<Double> embedding = embeddingService.getEmbedding(chunkText, "search_document: ");

                // store in vector DB
                Chunk chunkObj = new Chunk(chunkText, embedding, fileName);
                vectorDB.addChunk(chunkObj);
                count++;

                // prevent infinite loop if overlap >= size (should not happen with hardcoded
                // values)
                if (i + (size - overlap) <= i)
                    break;
            }

            return "PDF parsed successfully. Chunks created and stored in VectorDB: " + count;

        } catch (Exception e) {
            e.printStackTrace();
            return "Error uploading: " + e.getMessage();
        }
    }

    @GetMapping
    public java.util.Set<String> getDocuments() {
        return vectorDB.listDocuments();
    }
}
