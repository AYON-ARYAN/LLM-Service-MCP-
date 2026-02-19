package com.mcp.ragmcp.service;

import org.springframework.stereotype.Service;

@Service
public class AgentService {

    private final GroqService groqService;
    private final SerpService serpService;

    public AgentService(GroqService groqService, SerpService serpService) {
        this.groqService = groqService;
        this.serpService = serpService;
    }

    public String searchInternet(String question) {
        System.out.println("---- SERP SEARCH ----");

        String googleData = serpService.searchGoogle(question);

        System.out.println("SERP DATA:\n" + googleData);

        String finalPrompt = "You are a highly capable AI assistant with access to real-time web search results.\n"
                +
                "Your goal is to answer the User's question accurately using ONLY the provided metadata.\n\n" +

                "SEARCH RESULTS (SERP DATA):\n" + googleData + "\n\n" +

                "INSTRUCTIONS:\n" +
                "1. ANALYZE the search snippets carefully. Look for consensus among sources.\n" +
                "2. If snippets provide conflicting data, MENTION the conflict and provide the range or options found.\n"
                +
                "3. USE YOUR REASONING to interpret the user's intent even if the query is imperfect (e.g., 'int' -> 'INR').\n"
                +
                "4. If exact details are missing, provide the best available info from the snippets and explain the limitation politely.\n"
                +
                "5. For fast-changing data, prefer the most recent snippets.\n" +
                "6. Be helpful and direct. Avoid repeating 'I couldn't find specific details' unless truly nothing is relevant.\n\n"
                +

                "USER QUESTION: " + question;

        return groqService.askGroq(finalPrompt);
    }

    public String decide(String question) {
        String lowerQ = question.toLowerCase();

        // 1. REGEX HEURISTICS
        if (lowerQ.contains("weather") || lowerQ.contains("news") ||
                lowerQ.contains("price") || lowerQ.contains("location") ||
                lowerQ.contains("update") || lowerQ.contains("latest")) {
            return "SEARCH";
        }

        // 2. CONTEXT AWARENESS (Pronouns)
        // matches "it", "that", "he", "she", "they", "company", "bike" as whole words
        if (lowerQ.matches(".*\\b(it|that|he|she|they|company|bike)\\b.*")) {
            return "CONTEXT_CHAT";
        }

        // 3. LLM FALLBACK
        String prompt = """
                You are an AI routing brain.

                Decide BEST tool for the user query.

                TOOLS:
                SEARCH -> realtime info (weather, news, current events, internet)
                RAG -> question about uploaded document
                SUMMARY -> summarize uploaded document
                MEMORY -> question about previous chat
                CHAT -> normal conversation

                STRICT RULES:
                - weather, news, current info -> SEARCH
                - questions about document -> RAG
                - "summarize" -> SUMMARY
                - personal conversation -> CHAT
                - previous conversation -> MEMORY

                Return ONLY ONE WORD:
                SEARCH or RAG or SUMMARY or CHAT or MEMORY

                User Query: "%s"
                """.formatted(question);

        String decision = groqService.askGroq(prompt).trim().toUpperCase();

        if (!decision.equals("SEARCH") &&
                !decision.equals("RAG") &&
                !decision.equals("SUMMARY") &&
                !decision.equals("MEMORY")) {
            decision = "CHAT";
        }

        return decision;
    }

    public String contextualize(String history, String question) {
        String prompt = """
                Given the following conversation history and a new user question, rewrite the question to be a standalone query that contains all necessary context.
                Resolving pronouns (it, he, she, they) is CRITICAL.

                CHAT HISTORY:
                %s

                USER QUESTION:
                %s

                REWRITTEN QUESTION (return ONLY the question):
                """
                .formatted(history, question);

        String rewritten = groqService.askGroq(prompt).trim();
        // Fallback if LLM fails or returns empty
        if (rewritten.isEmpty() || rewritten.length() < 5) {
            return question;
        }
        // remove quotes if LLM adds them
        rewritten = rewritten.replace("\"", "");
        System.out.println("Wait ... Contextualizing Query: " + question + " -> " + rewritten);
        return rewritten;
    }
}