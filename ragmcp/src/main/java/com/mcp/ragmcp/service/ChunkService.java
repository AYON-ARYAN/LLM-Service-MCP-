package com.mcp.ragmcp.service;

import com.mcp.ragmcp.model.Chunk;
import com.mcp.ragmcp.service.EmbeddingService;
import com.mcp.ragmcp.service.VectorDBService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class ChunkService {

    @Autowired
    EmbeddingService embeddingService;

    @Autowired
    VectorDBService vectorDB;

    // cosine similarity
    private double similarity(List<Double> a, List<Double> b) {

        if (a.isEmpty() || b.isEmpty())
            return 0;

        int size = Math.min(a.size(), b.size());

        double dot = 0, normA = 0, normB = 0;

        for (int i = 0; i < size; i++) {
            double valA = a.get(i);
            double valB = b.get(i);
            dot += valA * valB;
            normA += valA * valA;
            normB += valB * valB;
        }

        if (normA == 0 || normB == 0)
            return 0;

        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    // search top chunks
    public String search(String question) {

        List<Chunk> database = vectorDB.getAll();

        if (database.isEmpty())
            return "";

        List<Double> qEmb = embeddingService.getEmbedding(question, "search_query: ");

        if (qEmb.isEmpty()) {
            System.out.println("Question embedding failed");
            return "Embedding failed.";
        }

        System.out.println("Embedding size: " + qEmb.size());

        // Helper class for sorting
        class ScoredChunk {
            Chunk chunk;
            double score;

            ScoredChunk(Chunk c, double s) {
                this.chunk = c;
                this.score = s;
            }
        }

        List<ScoredChunk> scoredChunks = new ArrayList<>();

        for (Chunk chunk : database) {
            double score = similarity(qEmb, chunk.embedding);
            scoredChunks.add(new ScoredChunk(chunk, score));
        }

        // Sort descending by score
        scoredChunks.sort((a, b) -> Double.compare(b.score, a.score));

        // Get top 10 (or fewer if not enough chunks)
        int topK = Math.min(10, scoredChunks.size());

        StringBuilder context = new StringBuilder();
        context.append("Use this document context:\n\n");

        for (int i = 0; i < topK; i++) {
            ScoredChunk sc = scoredChunks.get(i);
            System.out.println("Match " + (i + 1) + ": Score " + sc.score + " - "
                    + sc.chunk.text.substring(0, Math.min(50, sc.chunk.text.length())) + "...");
            context.append(sc.chunk.text).append("\n---\n");
        }

        return context.toString();
    }
}