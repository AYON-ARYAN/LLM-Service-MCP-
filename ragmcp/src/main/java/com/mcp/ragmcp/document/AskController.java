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
    @Autowired
    com.mcp.ragmcp.service.ContextService contextService;
    @Autowired
    com.mcp.ragmcp.service.AgentLoopService agentLoop;

    @CrossOrigin(origins = "http://localhost:3000") // Allow Next.js
    @PostMapping(value = { "", "/question" })
    public java.util.Map<String, Object> askQuestion(@RequestBody String question) {

        memory.addMessage("User", question);

        java.util.Map<String, String> agentResult = agentLoop.runAgent(question);

        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("answer", agentResult.get("answer"));
        result.put("thoughts", agentResult.get("thoughts")); // Full Log (Status)
        result.put("reasoning", agentResult.get("reasoning")); // One-line thought
        result.put("intent", "AGENT_LOOP");

        return result;
    }
}