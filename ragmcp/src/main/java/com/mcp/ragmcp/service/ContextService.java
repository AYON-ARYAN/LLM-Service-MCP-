package com.mcp.ragmcp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ContextService {

    @Autowired
    ChatMemoryService memory;

    @Autowired
    GroqService groqService;

    public String resolve(String question) {

        String history = memory.getFormattedHistory();

        String prompt = "Based on conversation:\n"
                + history +
                "\nRewrite this question clearly:\n"
                + question +
                "\nReturn only improved question.";

        return groqService.askGroq(prompt);
    }
}
