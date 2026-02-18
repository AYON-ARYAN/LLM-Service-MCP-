package com.mcp.ragmcp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class SerpService {

    @Value("${serp.api.key}")
    private String API_KEY;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String searchGoogle(String query) {
        System.out.println("DEBUG: searchGoogle query=" + query);
        String safeQuery = query.replaceAll("[^a-zA-Z0-9\\s]", "");
        String lowerQuery = safeQuery.toLowerCase();

        // 1. NEWS (Freshness logic)
        if (lowerQuery.contains("news") || lowerQuery.contains("latest")) {
            String params = "&tbm=nws&tbs=qdr:w";
            if (lowerQuery.contains("latest") || lowerQuery.contains("today") || lowerQuery.contains("24 hours")
                    || lowerQuery.contains("24h") || lowerQuery.contains("recent")) {
                params = "&tbm=nws&tbs=qdr:d";
            }
            return searchEngine("google", safeQuery, params);
        }

        // 2. PRODUCT SUGGESTIONS / REVIEWS (Prioritize over Shopping for broad queries)
        // "Suggest phone", "Best laptop", "Help me shop", "Budget..." -> Standard
        // Google Search + Past Year
        if (lowerQuery.contains("suggest") || lowerQuery.contains("best") || lowerQuery.contains("top ")
                || lowerQuery.contains("review") || lowerQuery.contains("under") || lowerQuery.contains("help")
                || lowerQuery.contains("budget")) {
            return searchEngine("google", safeQuery, "&tbs=qdr:y");
        }

        // 3. SHOPPING (Buy/Price) - Only for explicit shopping
        if (lowerQuery.contains("buy") || lowerQuery.contains("price")) {
            return searchEngine("google_shopping", safeQuery, "");
        }

        // 4. SCHOLAR
        if (lowerQuery.contains("scholar") || lowerQuery.contains("paper") || lowerQuery.contains("thesis")) {
            return searchEngine("google_scholar", safeQuery, "");
        }

        // 5. HOTELS (Revert to Standard Google Search for robustness)
        // google_hotels engine requires specific params and errors on natural language.
        // Standard Google Search handles "Hotels in Patna" well via Local Pack.
        if (lowerQuery.contains("hotel") || lowerQuery.contains("resort")) {
            return searchEngine("google", safeQuery, "");
        }

        // 6. FLIGHTS (Use Google Search with Past Month filter)
        // Standard Google search often returns "Answer Box" or "Knowledge Graph" for
        // flights.
        // qdr:m ensures we don't get 1-year old blog posts about "Cheap Flights".
        if (lowerQuery.contains("flight") || lowerQuery.contains("fly ") || lowerQuery.contains("ticket")) {
            return searchEngine("google", safeQuery, "&tbs=qdr:m");
        }

        // 7. WEATHER
        if (lowerQuery.contains("weather")) {
            return searchEngine("google", safeQuery, "");
        }

        // 8. DEFAULT
        return searchEngine("google", safeQuery, "");
    }

    private String searchEngine(String engine, String query, String extraParams) {
        try {
            String urlString = "https://serpapi.com/search.json?engine=" + engine + "&q="
                    + query.replace(" ", "+")
                    + "&api_key=" + API_KEY
                    + "&gl=in" // Google Locale: India
                    + "&hl=en" // Google Host Language: English
                    + "&currency=INR" // Currency: Indian Rupee
                    + extraParams;

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder responseBuilder = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null)
                responseBuilder.append(line);
            br.close();

            JsonNode root = objectMapper.readTree(responseBuilder.toString());
            StringBuilder resultText = new StringBuilder();

            // --- ENGINE SPECIFIC PARSING ---

            // NEWS (When using google_news OR google with tbm=nws)
            if (root.has("news_results")) {
                for (JsonNode news : root.get("news_results")) {
                    if (news.has("title"))
                        resultText.append("Headline: ").append(news.get("title").asText()).append("\n");
                    if (news.has("snippet"))
                        resultText.append("Snippet: ").append(news.get("snippet").asText()).append("\n");
                    if (news.has("date"))
                        resultText.append("Date: ").append(news.get("date").asText()).append("\n---\n");
                    if (news.has("source"))
                        resultText.append("Source: ").append(news.get("source").asText()).append("\n---\n");
                }
            }

            // SHOPPING
            else if (engine.equals("google_shopping") && root.has("shopping_results")) {
                for (JsonNode item : root.get("shopping_results")) {
                    if (item.has("title"))
                        resultText.append("Product: ").append(item.get("title").asText()).append("\n");
                    if (item.has("price"))
                        resultText.append("Price: ").append(item.get("price").asText()).append("\n");
                    if (item.has("source"))
                        resultText.append("Store: ").append(item.get("source").asText()).append("\n---\n");
                }
            }

            // SCHOLAR
            else if (engine.equals("google_scholar") && root.has("organic_results")) {
                for (JsonNode paper : root.get("organic_results")) {
                    if (paper.has("title"))
                        resultText.append("Title: ").append(paper.get("title").asText()).append("\n");
                    if (paper.has("snippet"))
                        resultText.append("Snippet: ").append(paper.get("snippet").asText()).append("\n---\n");
                }
            }

            // DEFAULT (Google Search - Handles Web, Flights, Hotels via Local Pack)
            else {
                // 1. Answer Box (Now handles Weather too)
                if (root.has("answer_box")) {
                    JsonNode ab = root.get("answer_box");
                    if (ab.has("title"))
                        resultText.append("Answer Box: ").append(ab.get("title").asText()).append("\n");
                    if (ab.has("snippet"))
                        resultText.append(ab.get("snippet").asText()).append("\n");
                    if (ab.has("answer"))
                        resultText.append(ab.get("answer").asText()).append("\n");

                    // Weather specific fields
                    if (ab.has("temperature"))
                        resultText.append("Temperature: ").append(ab.get("temperature").asText()).append("\n");
                    if (ab.has("weather"))
                        resultText.append("Condition: ").append(ab.get("weather").asText()).append("\n");
                    if (ab.has("humidity"))
                        resultText.append("Humidity: ").append(ab.get("humidity").asText()).append("\n");
                    if (ab.has("precipitation"))
                        resultText.append("Precipitation: ").append(ab.get("precipitation").asText()).append("\n");
                    if (ab.has("wind"))
                        resultText.append("Wind: ").append(ab.get("wind").asText()).append("\n");
                }

                // 2. Knowledge Graph
                if (root.has("knowledge_graph")) {
                    JsonNode kg = root.get("knowledge_graph");
                    if (kg.has("title"))
                        resultText.append("Topic: ").append(kg.get("title").asText()).append("\n");
                    if (kg.has("description"))
                        resultText.append(kg.get("description").asText()).append("\n");
                }

                // 3. Local Results (Crucial for Hotels / Places)
                if (root.has("local_results")) {
                    if (root.get("local_results").has("places")) { // sometimes nested
                        for (JsonNode place : root.get("local_results").get("places")) {
                            if (place.has("title"))
                                resultText.append("Place: ").append(place.get("title").asText()).append("\n");
                            if (place.has("rating"))
                                resultText.append("Rating: ").append(place.get("rating").asText()).append("\n");
                            if (place.has("address"))
                                resultText.append("Address: ").append(place.get("address").asText()).append("\n---\n");
                        }
                    } else if (root.get("local_results").isArray()) { // standard array
                        for (JsonNode place : root.get("local_results")) {
                            if (place.has("title"))
                                resultText.append("Place: ").append(place.get("title").asText()).append("\n");
                            if (place.has("rating"))
                                resultText.append("Rating: ").append(place.get("rating").asText()).append("\n");
                            if (place.has("address"))
                                resultText.append("Address: ").append(place.get("address").asText()).append("\n---\n");
                        }
                    }
                }

                // 4. Organic Results
                if (root.has("organic_results")) {
                    int count = 0;
                    for (JsonNode res : root.get("organic_results")) {
                        if (res.has("snippet")) {
                            resultText.append(res.get("snippet").asText()).append("\n");
                            count++;
                        }
                        if (count >= 5)
                            break;
                    }
                }
            }

            if (resultText.length() == 0)
                return "No specific results found for " + engine;
            return resultText.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "SERP ERROR: " + e.getMessage();
        }
    }
}