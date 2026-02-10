package com.uairouter;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import androidx.room.Room;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import org.json.JSONObject;

public class AnalyticsActivity extends Activity implements BackendSyncService.SyncCallback {
    private TextView tv;
    private Handler handler = new Handler(Looper.getMainLooper());
    private AppDatabase db;
    private TenantManager tenantManager;
    private String currentTenantId;
    private BackendSyncService backendSyncService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        tv = findViewById(R.id.analytics_info);

        // Initialize tenant management
        tenantManager = new TenantManager(this);
        currentTenantId = tenantManager.getCurrentTenantId();

        // Initialize Room database
        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "uai-router-db").build();

        // Get backend sync service from application context (assuming it's a singleton)
        // For now, create a new instance - in production this should be a singleton
        OAuth2Service oauth2Service = new OAuth2Service(this);
        backendSyncService = new BackendSyncService(this, oauth2Service, tenantManager);
        backendSyncService.registerCallback("analytics", this);

        // Load cached data first
        loadCachedData();

        // Connect to backend analytics service
        backendSyncService.connect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (backendSyncService != null) {
            backendSyncService.unregisterCallback("analytics");
        }
        if (db != null && !db.isOpen()) {
            db.close();
        }
    }

    @Override
    public void onDataReceived(String endpoint, JSONObject data) {
        if ("analytics".equals(endpoint)) {
            try {
                // Cache the data with tenant ID
                AnalyticsData analyticsData = new AnalyticsData(
                        currentTenantId,
                        data.getInt("total_sessions"),
                        data.getInt("active_users"),
                        data.getDouble("data_processed_gb"),
                        data.getInt("ai_insights_generated"),
                        data.getDouble("system_uptime_percent"),
                        data.getString("last_updated")
                );
                new Thread(() -> db.analyticsDao().insertAnalytics(analyticsData)).start();

                String info = getString(R.string.analytics_dashboard) + " (Live)\n\n" +
                        getString(R.string.total_sessions, data.getInt("total_sessions")) + "\n" +
                        getString(R.string.active_users, data.getInt("active_users")) + "\n" +
                        getString(R.string.data_processed, data.getDouble("data_processed_gb")) + "\n" +
                        getString(R.string.ai_insights, data.getInt("ai_insights_generated")) + "\n" +
                        getString(R.string.system_uptime, data.getDouble("system_uptime_percent")) + "\n" +
                        getString(R.string.last_updated, data.getString("last_updated"));
                handler.post(() -> tv.setText(info));
            } catch (Exception e) {
                handler.post(() -> tv.setText(getString(R.string.error_parse, e.getMessage())));
            }
        }
    }

    @Override
    public void onConnectionStatusChanged(String endpoint, boolean connected) {
        if ("analytics".equals(endpoint)) {
            handler.post(() -> {
                if (connected) {
                    if (tv.getText().toString().contains("(Cached)")) {
                        tv.setText(tv.getText().toString().replace(" (Cached)", " (Live)"));
                    } else {
                        tv.setText(getString(R.string.connected_realtime));
                    }
                } else {
                    if (tv.getText().toString().contains("(Live)")) {
                        tv.setText(tv.getText().toString().replace(" (Live)", " (Offline)"));
                    } else {
                        tv.setText(getString(R.string.connection_closed));
                    }
                }
            });
        }
    }

    @Override
    public void onError(String endpoint, String error) {
        if ("analytics".equals(endpoint)) {
            handler.post(() -> {
                if (tv.getText().toString().contains("(Live)")) {
                    tv.setText(tv.getText().toString().replace(" (Live)", " (Error)"));
                } else {
                    tv.setText(getString(R.string.connection_failed, error));
                }
            });
        }
    }

    private void loadCachedData() {
        new Thread(() -> {
            AnalyticsData cached = db.analyticsDao().getLatestAnalytics(currentTenantId);
            if (cached != null) {
                String info = getString(R.string.analytics_dashboard) + " (Cached)\n\n" +
                        getString(R.string.total_sessions, cached.totalSessions) + "\n" +
                        getString(R.string.active_users, cached.activeUsers) + "\n" +
                        getString(R.string.data_processed, cached.dataProcessedGb) + "\n" +
                        getString(R.string.ai_insights, cached.aiInsightsGenerated) + "\n" +
                        getString(R.string.system_uptime, cached.systemUptimePercent) + "\n" +
                        getString(R.string.last_updated, cached.lastUpdated);
                handler.post(() -> tv.setText(info));
            } else {
                handler.post(() -> tv.setText(getString(R.string.loading)));
            }
        }).start();
    }
}
