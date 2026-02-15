package com.mcp.ragmcp.service;

import org.springframework.stereotype.Service;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class GroqService {

    @org.springframework.beans.factory.annotation.Value("${groq.api.key}")
    private String API_KEY;

    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

    public String askGroq(String document, String question) {
        String prompt = "Answer ONLY using this document:\n\n"
                + document
                + "\n\nUser question: "
                + question;
        return askGroq(prompt);
    }

    // Overloaded method for single prompt
    public String askGroq(String prompt) {
        try {
            URL url = new URL("https://api.groq.com/openai/v1/chat/completions");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // Escape the prompt properly for JSON
            String jsonInput = objectMapper.writeValueAsString(java.util.Map.of(
                    "model", "llama-3.3-70b-versatile",
                    "temperature", 0.3,
                    "messages", java.util.List.of(
                            java.util.Map.of("role", "user", "content", prompt))));

            OutputStream os = conn.getOutputStream();
            os.write(jsonInput.getBytes());
            os.flush();
            os.close();

            // if error, read error stream
            InputStream is;
            if (conn.getResponseCode() >= 400) {
                is = conn.getErrorStream();
            } else {
                is = conn.getInputStream();
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuilder responseBuilder = new StringBuilder();

            while ((line = br.readLine()) != null) {
                responseBuilder.append(line);
            }

            String jsonResponse = responseBuilder.toString();

            // Parse response to extract content
            com.fasterxml.jackson.databind.JsonNode rootNode = objectMapper.readTree(jsonResponse);
            if (rootNode.has("choices") && rootNode.get("choices").isArray() && rootNode.get("choices").size() > 0) {
                return rootNode.get("choices").get(0).get("message").get("content").asText();
            }

            return "Error: No content in response: " + jsonResponse;

        } catch (Exception e) {
            e.printStackTrace();
            return "Groq error: " + e.getMessage();
        }
    }
}