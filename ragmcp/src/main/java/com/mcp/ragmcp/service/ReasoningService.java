package com.mcp.ragmcp.service;

import org.springframework.stereotype.Service;

@Service
public class ReasoningService {

    public String buildReasoningPrompt(String context, String question) {
        return """
                You are a highly intelligent and analytical research assistant.

                Your goal is to answer the user's question by synthesizing information from the provided context.

                CONTEXT:
                ---------------------
                %s
                ---------------------

                USER QUESTION:
                %s

                INSTRUCTIONS:
                1. carefully read the context sections.
                2. Identify key information relevant to the user's question.
                3. Connect different pieces of information if they are related.
                4. Answer the question comprehensively.
                5. If the context does not contain the answer, state that clearly, but try to provide any relevant context that *is* available.

                Think step-by-step before answering.

                Answer:
                """
                .formatted(context, question);
    }

    public String buildGodPrompt(String context, String history, String question) {
        return """
                You are a superintelligent AI.

                Conversation:
                """ + history + """

                Document context:
                """ + context + """

                User question:
                """ + question + """

                Steps:
                1. Understand conversation context
                2. Understand user intent
                3. If real-time info needed -> assume search data
                4. Think step by step
                5. Give final professional answer
                6. If context missing -> answer using general knowledge.

                Return only final answer.
                """;
    }
}