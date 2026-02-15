package com.mcp.ragmcp.service;

import okhttp3.*;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class EmbeddingService {

    public List<Double> getEmbedding(String text, String prefix) {

        List<Double> vector = new ArrayList<>();

        try {

            OkHttpClient client = new OkHttpClient();

            // Prepare JSON request using a Map to ensure correct escaping
            Map<String, Object> requestBodyMap = new HashMap<>();
            requestBodyMap.put("model", "nomic-embed-text");
            requestBodyMap.put("prompt", prefix + text);

            String json = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(requestBodyMap);

            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(json, mediaType);

            Request request = new Request.Builder()
                    .url("http://localhost:11434/api/embeddings")
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .build();

            Response response = client.newCall(request).execute();
            String res = response.body().string();

            // Use Jackson to parse the response
            com.fasterxml.jackson.databind.JsonNode rootNode = new com.fasterxml.jackson.databind.ObjectMapper()
                    .readTree(res);
            com.fasterxml.jackson.databind.JsonNode embeddingNode = rootNode.path("embedding");

            if (embeddingNode.isArray()) {
                for (com.fasterxml.jackson.databind.JsonNode node : embeddingNode) {
                    vector.add(node.asDouble());
                }
            }

            System.out.println("Embedding size: " + vector.size() + " for prefix: " + prefix);

        } catch (Exception e) {
            System.err.println("Embedding error: " + e.getMessage());
        }

        return vector;
    }
}
