package com.mcp.ragmcp.controller;

import com.mcp.ragmcp.service.ChunkService;
import com.mcp.ragmcp.service.GroqService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.*;

@RestController
@RequestMapping("/mcp")
@CrossOrigin(origins = "*")
public class MCPController {

    @Autowired
    GroqService groqService;
    @Autowired
    ChunkService chunkService;

    @PostMapping
    public Map<String, Object> handle(@RequestBody Map<String, Object> request) {

        String method = (String) request.get("method");

        // 🔥 tool listing
        if ("tools/list".equals(method)) {
            return Map.of(
                    "tools", List.of(
                            Map.of(
                                    "name", "askDocument",
                                    "description", "Ask questions from uploaded document")));
        }

        // 🔥 tool calling
        if ("tools/call".equals(method)) {
            Map params = (Map) request.get("params");
            String question = (String) params.get("question");

            String docContext = chunkService.search(question);

            if (docContext == null || docContext.isEmpty()) {
                return Map.of("error", "No relevant context found or no documents uploaded");
            }

            String answer = groqService.askGroq(docContext, question);

            return Map.of("result", answer);
        }

        return Map.of("error", "Unknown method");
    }
}