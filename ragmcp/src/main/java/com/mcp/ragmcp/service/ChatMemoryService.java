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

    public String getFormattedHistory() {
        return getFormattedHistory(10); // Default limit
    }

    public String getFormattedHistory(int limit) {
        StringBuilder sb = new StringBuilder();
        int start = Math.max(0, chatHistory.size() - limit);
        for (int i = start; i < chatHistory.size(); i++) {
            sb.append(chatHistory.get(i)).append("\n");
        }
        return sb.toString();
    }

    public void clear() {
        chatHistory.clear();
    }
}