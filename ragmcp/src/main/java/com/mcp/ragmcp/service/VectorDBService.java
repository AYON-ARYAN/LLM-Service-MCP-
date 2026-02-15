package com.mcp.ragmcp.service;

import com.mcp.ragmcp.model.Chunk;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class VectorDBService {

    public List<Chunk> database = new ArrayList<>();

    public void addChunk(Chunk c) {
        database.add(c);
    }

    public List<Chunk> getAll() {
        return database;
    }

    public String getFullDocument() {
        StringBuilder fullDoc = new StringBuilder();
        for (Chunk chunk : database) {
            fullDoc.append(chunk.text).append("\n");
        }
        return fullDoc.toString();
    }

    public int size() {
        System.out.println("Total chunks in DB: " + database.size());
        return database.size();
    }

    public java.util.Set<String> listDocuments() {
        java.util.Set<String> docs = new java.util.HashSet<>();
        for (Chunk c : database) {
            docs.add(c.documentName);
        }
        return docs;
    }
}