package com.uai.router.services.ai;

public class ClaudeSonnetUsageStats {
    private final long totalTokens;
    private final long promptTokens;
    private final long completionTokens;
    private final double totalCost;

    public ClaudeSonnetUsageStats(long totalTokens, long promptTokens, long completionTokens, double totalCost) {
        this.totalTokens = totalTokens;
        this.promptTokens = promptTokens;
        this.completionTokens = completionTokens;
        this.totalCost = totalCost;
    }

    public long getTotalTokens() {
        return totalTokens;
    }

    public long getPromptTokens() {
        return promptTokens;
    }

    public long getCompletionTokens() {
        return completionTokens;
    }

    public double getTotalCost() {
        return totalCost;
    }
}
