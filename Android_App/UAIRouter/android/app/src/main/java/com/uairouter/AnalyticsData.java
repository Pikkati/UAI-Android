package com.uairouter;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "analytics_data")
public class AnalyticsData {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String tenantId; // Multi-tenant support
    public int totalSessions;
    public int activeUsers;
    public double dataProcessedGb;
    public int aiInsightsGenerated;
    public double systemUptimePercent;
    public String lastUpdated;
    public long timestamp; // When cached

    public AnalyticsData(String tenantId, int totalSessions, int activeUsers, double dataProcessedGb,
                        int aiInsightsGenerated, double systemUptimePercent, String lastUpdated) {
        this.tenantId = tenantId;
        this.totalSessions = totalSessions;
        this.activeUsers = activeUsers;
        this.dataProcessedGb = dataProcessedGb;
        this.aiInsightsGenerated = aiInsightsGenerated;
        this.systemUptimePercent = systemUptimePercent;
        this.lastUpdated = lastUpdated;
        this.timestamp = System.currentTimeMillis();
    }
}
