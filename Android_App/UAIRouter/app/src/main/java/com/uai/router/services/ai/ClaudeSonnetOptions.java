package com.uai.router.services.ai;

public class ClaudeSonnetOptions {
    private String modelVersion = "sonnet-3.5";
    private int maxTokens = 4096;
    private float temperature = 0.7f;
    private float topP = 1.0f;
    private String[] stopSequences;

    public ClaudeSonnetOptions() {}

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public float getTopP() {
        return topP;
    }

    public void setTopP(float topP) {
        this.topP = topP;
    }

    public String[] getStopSequences() {
        return stopSequences;
    }

    public void setStopSequences(String[] stopSequences) {
        this.stopSequences = stopSequences;
    }

    public static class Builder {
        private ClaudeSonnetOptions options = new ClaudeSonnetOptions();

        public Builder modelVersion(String modelVersion) {
            options.setModelVersion(modelVersion);
            return this;
        }

        public Builder maxTokens(int maxTokens) {
            options.setMaxTokens(maxTokens);
            return this;
        }

        public Builder temperature(float temperature) {
            options.setTemperature(temperature);
            return this;
        }

        public Builder topP(float topP) {
            options.setTopP(topP);
            return this;
        }

        public Builder stopSequences(String[] stopSequences) {
            options.setStopSequences(stopSequences);
            return this;
        }

        public ClaudeSonnetOptions build() {
            return options;
        }
    }
}
