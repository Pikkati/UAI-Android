package com.uai.router.services.ai;

public class ClaudeSonnetResponse {
    private final String text;
    private final int tokensUsed;
    private final double cost;

    public ClaudeSonnetResponse(String text, int tokensUsed, double cost) {
        this.text = text;
        this.tokensUsed = tokensUsed;
        this.cost = cost;
    }

    public String getText() {
        return text;
    }

    public int getTokensUsed() {
        return tokensUsed;
    }

    public double getCost() {
        return cost;
    }
}
