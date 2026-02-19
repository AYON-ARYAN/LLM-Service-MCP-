package com.mcp.ragmcp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AgentLoopService {

    @Autowired
    GroqService groq;
    @Autowired
    ChunkService chunkService;
    @Autowired
    SerpService serpService;
    @Autowired
    ChatMemoryService memory;
    @Autowired
    VectorDBService vectorDB;

    public java.util.Map<String, String> runAgent(String question) {

        String thoughts = "";
        String context = "";
        String toolResult = "";
        String finalAnswer = "";
        String originalQuestion = question;
        String latestReasoning = "Thinking..."; // Default reasoning

        // 🔁 LOOP (max 3 cycles)
        for (int step = 1; step <= 3; step++) {

            System.out.println("🧠 AGENT STEP " + step);

            // Limited history (last 6 messages) and concise prompt to save tokens
            String brainPrompt = """
                    Act as an autonomous AI.

                    History:
                    """ + memory.getFormattedHistory(6) +

                    """

                            Question:
                            """ + originalQuestion +

                    """

                            Context:
                            """ + context +

                    """

                            Tools:
                            1. SEARCH (realtime)
                            2. RAG (docs)
                            3. CHAT (knowledge)

                            Instructions:
                            - Resolve pronouns (e.g., "it" -> "Suzuki bikes") using History.
                            - Select tool based on *resolved* intent.

                            Reply ONLY:
                            TOOL: SEARCH/RAG/CHAT/FINAL
                            THOUGHT: brief reasoning
                            QUERY: search/question
                            """;

            String brain = groq.askGroq(brainPrompt);
            System.out.println("🧠 BRAIN:\n" + brain);

            // Extract THOUGHT for the "Reasoning" UI field
            if (brain.contains("THOUGHT:")) {
                int start = brain.indexOf("THOUGHT:") + 8;
                int end = brain.indexOf("\n", start);
                if (end == -1)
                    end = brain.length();
                latestReasoning = brain.substring(start, end).trim();
            }

            thoughts += "\n--- STEP " + step + " ---\n" + brain + "\n";

            // 🔍 Decide tool
            if (brain.contains("TOOL: FINAL")) {
                finalAnswer = brain;
                thoughts += "ACTION: FINAL ANSWER\n";
                break;
            }

            if (brain.contains("SEARCH")) {
                toolResult = serpService.searchGoogle(originalQuestion);
                context += "\nSEARCH RESULT:\n" + toolResult;
                thoughts += "ACTION: SEARCH\nRESULT: " + toolResult.substring(0, Math.min(toolResult.length(), 200))
                        + "...\n";
            }

            else if (brain.contains("RAG")) {
                toolResult = chunkService.search(originalQuestion);
                context += "\nDOC CONTEXT:\n" + toolResult;
                thoughts += "ACTION: RAG\nRESULT: " + toolResult.substring(0, Math.min(toolResult.length(), 200))
                        + "...\n";
            }

            else {
                toolResult = "";
                thoughts += "ACTION: CHAT (No tool)\n";
            }

            // add tool result to memory
            memory.addMessage("Tool", toolResult);

            // 🔁 Next thinking with context
            question = "Using this new info:\n" + toolResult + "\nAnswer original question.";
        }

        // 🧠 FINAL ANSWER
        if (finalAnswer.isEmpty() || finalAnswer.contains("TOOL: FINAL")) {
            // Limited history for final answer too
            String finalPrompt = """
                    Answer professionally using history and context.

                    History:
                    """ + memory.getFormattedHistory(6) + """

                    Context:
                    """ + context + """

                    Question:
                    """ + originalQuestion;

            finalAnswer = groq.askGroq(finalPrompt);
        }

        memory.addMessage("AI", finalAnswer);

        java.util.Map<String, String> response = new java.util.HashMap<>();
        response.put("answer", finalAnswer);
        response.put("thoughts", thoughts);
        response.put("reasoning", latestReasoning);
        return response;
    }
}
