package com.mcp.ragmcp.service;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class ChatMemoryService {

    private List<String> chatHistory = new ArrayList<>();

    public void addMessage(String role, String message) {
        chatHistory.add(role + ": " + message);
    }

    public String getHistory() {
        StringBuilder sb = new StringBuilder();

        for (String msg : chatHistory) {
            sb.append(msg).append("\n");
        }

        return sb.toString();
    }

    public void clear() {
        chatHistory.clear();
    }
}