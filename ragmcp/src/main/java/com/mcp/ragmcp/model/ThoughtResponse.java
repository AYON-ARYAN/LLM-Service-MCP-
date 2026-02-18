package com.mcp.ragmcp.model;

public class ThoughtResponse {

    public String intent;
    public String tool;
    public String reasoning;
    public String context;
    public String finalAnswer;

    public ThoughtResponse(String intent, String tool, String reasoning, String context, String finalAnswer) {
        this.intent = intent;
        this.tool = tool;
        this.reasoning = reasoning;
        this.context = context;
        this.finalAnswer = finalAnswer;
    }
}