package com.uairouter;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {AnalyticsData.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract AnalyticsDao analyticsDao();
}
