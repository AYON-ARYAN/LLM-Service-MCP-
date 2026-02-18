package com.mcp.ragmcp.document;

import com.mcp.ragmcp.service.AgentService;
import com.mcp.ragmcp.service.ChatMemoryService;
import com.mcp.ragmcp.service.ChunkService;
import com.mcp.ragmcp.service.GroqService;
import com.mcp.ragmcp.service.ReasoningService;
import com.mcp.ragmcp.service.VectorDBService;
import com.mcp.ragmcp.service.SerpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.mcp.ragmcp.model.ThoughtResponse;
import com.mcp.ragmcp.service.IntentService;

@RestController
@RequestMapping("/ask")
@CrossOrigin(origins = "*")
public class AskController {
    @Autowired
    ChunkService chunkService;
    @Autowired
    GroqService groqService;
    @Autowired
    ChatMemoryService memory;
    @Autowired
    ReasoningService reasoningService;
    @Autowired
    AgentService agentService;
    @Autowired
    VectorDBService vectorDBService;
    @Autowired
    SerpService serpService;
    @Autowired
    IntentService intentService;

    @CrossOrigin(origins = "http://localhost:3000") // Allow Next.js
    @PostMapping
    public java.util.Map<String, Object> askQuestion(@RequestBody String question) {

        memory.addMessage("User", question);

        // 1. Contextualize the question (Rewrite using History)
        String history = memory.getHistory(); // You might need to limit this if it's too long
        String contextualizedQuestion = agentService.contextualize(history, question);
        System.out.println("ORIGINAL: " + question);
        System.out.println("CONTEXTUALIZED: " + contextualizedQuestion);

        // 2. Use the REWRITTEN question for Intent & Search
        String decision = intentService.getIntent(contextualizedQuestion);
        System.out.println("AGENT DECISION: " + decision);

        String response = "";
        String thoughts = "";
        String toolUsed = decision;

        if (decision.equals("SUMMARY")) {

            String fullDoc = vectorDBService.getFullDocument();
            response = groqService.askGroq("Summarize this:\n" + fullDoc);
            thoughts = "Analyzed intent: Document Summary.\n" +
                    "Action: Retrieved full document content from Vector DB (" + fullDoc.length() + " chars).\n" +
                    "Process: Sent full text to Groq LLM for summarization.";

        } else if (decision.equals("SEARCH")) {

            response = agentService.searchInternet(contextualizedQuestion);
            thoughts = "Analyzed user question: \"" + question + "\".\n" +
                    "Rewritten for context: \"" + contextualizedQuestion + "\".\n" +
                    "Identified intent: Real-time information search.\n" +
                    "Action: Queried SerpService to fetch live data from Google.\n" +
                    "Process: Passed raw SERP data to Groq LLM for synthesis and formatting.";

        } else if (decision.equals("RAG")) {

            String context = chunkService.search(contextualizedQuestion);
            // fix: chunkService.search returns a String context directly, which is correct
            String finalPrompt = reasoningService.buildReasoningPrompt(context, contextualizedQuestion);
            response = groqService.askGroq(finalPrompt);
            thoughts = "Analyzed intent: Specific Question on Document (RAG).\n" +
                    "Rewritten for context: \"" + contextualizedQuestion + "\".\n" +
                    "Action: Performed semantic search to retrieve top relevant chunks.\n" +
                    "Context: Found relevant information in document.\n" +
                    "Process: Constructed reasoning prompt with context and sent to Groq LLM.";

        } else {

            response = groqService.askGroq(question); // Chat can use original or rewritten
            thoughts = "Normal LLM chat";
        }

        memory.addMessage("AI", response);

        // RETURN CLEAN JSON
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("answer", response);
        result.put("intent", decision);
        result.put("tool", toolUsed);
        result.put("thoughts", thoughts);

        return result;
    }
}