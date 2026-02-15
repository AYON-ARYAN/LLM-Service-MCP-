package com.mcp.ragmcp.model;

import java.util.List;

public class Chunk {

    public String text;
    public List<Double> embedding;
    public String documentName;

    public Chunk(String text, List<Double> embedding, String documentName) {
        this.text = text;
        this.embedding = embedding;
        this.documentName = documentName;
    }
}