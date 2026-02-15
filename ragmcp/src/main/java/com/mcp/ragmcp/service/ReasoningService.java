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
}