package com.uai.router.services.ai;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

public class AndroidAnalytics {
    private static final String TAG = "UAICLAUDE.Analytics";

    public static void logUsage(Context ctx, long totalTokens, long promptTokens, long completionTokens, double totalCost) {
        try {
            File dir = ctx.getExternalFilesDir(null);
            if (dir == null) dir = ctx.getFilesDir();
            File file = new File(dir, "uai_claude_usage.jsonl");
            try (FileWriter fw = new FileWriter(file, true)) {
                String line = String.format("{\"timestamp\":\"%s\",\"totalTokens\":%d,\"promptTokens\":%d,\"completionTokens\":%d,\"totalCost\":%.4f}\n",
                        new Date().toString(), totalTokens, promptTokens, completionTokens, totalCost);
                fw.write(line);
            }
        } catch (IOException e) {
            Log.w(TAG, "Failed to write analytics", e);
        }
    }
}
