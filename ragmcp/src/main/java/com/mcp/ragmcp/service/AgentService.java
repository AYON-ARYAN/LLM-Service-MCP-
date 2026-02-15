package com.mcp.ragmcp.service;

import org.springframework.stereotype.Service;

@Service
public class AgentService {

    private final GroqService groqService;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    public AgentService(GroqService groqService) {
        this.groqService = groqService;
        this.objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
    }

    public String decide(String question) {
        String prompt = """
                You are a routing agent. logical reasoning agent
                Classify the user's question into exactly one of these categories:

                1. SUMMARY: User wants a summary, overview, or explanation of the entire document.
                2. RAG: User asks a specific question about the content of the document.
                3. GENERAL: User asks a general question, greeting, or meta-question unrelated to the document content.

                Reply ONLY with the category name (SUMMARY, RAG, or GENERAL). No other text.

                User question: "%s"
                """.formatted(question);

        try {
            String jsonResponse = groqService.askGroq(prompt);
            com.fasterxml.jackson.databind.JsonNode rootNode = objectMapper.readTree(jsonResponse);

            if (rootNode.has("choices") && rootNode.get("choices").isArray() && rootNode.get("choices").size() > 0) {
                String content = rootNode.get("choices").get(0).get("message").get("content").asText().trim();
                // Clean up any potential extra characters or markdown
                return content.replaceAll("[^A-Z]", "");
            }

        } catch (Exception e) {
            System.err.println("Agent decision error: " + e.getMessage());
        }

        // Fallback
        return "RAG";
    }
}