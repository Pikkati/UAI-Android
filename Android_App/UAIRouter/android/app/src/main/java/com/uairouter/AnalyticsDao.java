package com.uairouter;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface AnalyticsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAnalytics(AnalyticsData analytics);

    @Query("SELECT * FROM analytics_data WHERE tenantId = :tenantId ORDER BY timestamp DESC LIMIT 1")
    AnalyticsData getLatestAnalytics(String tenantId);

    @Query("DELETE FROM analytics_data WHERE tenantId = :tenantId AND timestamp < :olderThan")
    void deleteOldData(String tenantId, long olderThan);

    @Query("SELECT * FROM analytics_data WHERE tenantId = :tenantId")
    AnalyticsData[] getAllAnalyticsForTenant(String tenantId);
}
