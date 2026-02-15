package com.mcp.ragmcp.document;

import com.mcp.ragmcp.service.AgentService;
import com.mcp.ragmcp.service.ChatMemoryService;
import com.mcp.ragmcp.service.ChunkService;
import com.mcp.ragmcp.service.GroqService;
import com.mcp.ragmcp.service.ReasoningService;
import com.mcp.ragmcp.service.VectorDBService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;

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

    @PostMapping
    public Object askQuestion(@RequestBody String question) {

        StringBuilder thoughts = new StringBuilder();

        thoughts.append("Received question\n");

        memory.addMessage("User", question);

        thoughts.append("Saved to memory\n");

        String decision = agentService.decide(question);
        thoughts.append("Agent decision: ").append(decision).append("\n");

        String response = "";

        if (decision.equals("SUMMARY")) {

            thoughts.append("Fetching full document\n");

            String fullDoc = vectorDBService.getFullDocument();

            thoughts.append("Sending to LLM for summarization\n");

            response = groqService.askGroq("Summarize:\n" + fullDoc);

        } else if (decision.equals("RAG")) {

            thoughts.append("Performing vector search\n");

            String context = chunkService.search(question);

            thoughts.append("Context retrieved\n");

            String finalPrompt = reasoningService.buildReasoningPrompt(context, question);

            thoughts.append("Reasoning prompt built\n");

            response = groqService.askGroq(finalPrompt);

            thoughts.append("LLM response generated\n");

        } else {

            thoughts.append("Normal LLM mode\n");

            response = groqService.askGroq(question);
        }

        memory.addMessage("AI", response);
        thoughts.append("Saved response to memory\n");

        return java.util.Map.of(
                "answer", response,
                "thoughts", thoughts.toString());
    }
}