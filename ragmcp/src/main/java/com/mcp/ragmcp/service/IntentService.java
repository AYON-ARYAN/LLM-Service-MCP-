package com.mcp.ragmcp.service;

import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class IntentService {

    public String getIntent(String question) {
        try {
            System.out.println("🚀 CALLING PYTHON INTENT API...");

            URL url = new URL("http://localhost:5005/predict");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String cleanQuestion = question.replace("\"", "").replace("\n", " ");
            String jsonInput = "{ \"text\": \"" + cleanQuestion + "\" }";

            OutputStream os = conn.getOutputStream();
            os.write(jsonInput.getBytes("UTF-8"));
            os.flush();
            os.close();

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "UTF-8"));

            StringBuilder response = new StringBuilder();
            String line;

            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            br.close();

            String res = response.toString();
            System.out.println("🧠 PYTHON RAW RESPONSE: " + res);

            // 🔥 REAL JSON PARSE
            JSONObject obj = new JSONObject(res);
            String intent = obj.getString("intent");

            System.out.println("🧠 ML INTENT FINAL: " + intent);

            return intent;

        } catch (Exception e) {
            e.printStackTrace();
            return "CHAT";
        }
    }
}