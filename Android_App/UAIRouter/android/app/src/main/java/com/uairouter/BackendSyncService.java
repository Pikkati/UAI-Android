package com.uairouter;

import android.content.Context;
import android.util.Log;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import org.json.JSONObject;
import org.json.JSONException;
import java.util.concurrent.TimeUnit;
import java.util.HashMap;
import java.util.Map;

public class BackendSyncService {
    private static final String TAG = "BackendSyncService";
    private static final String BACKEND_WS_URL = "ws://10.0.2.2:8080";
    private static final String ANALYTICS_ENDPOINT = "/analytics";
    private static final String CLUSTER_ENDPOINT = "/clusters";
    private static final String SYSTEM_ENDPOINT = "/system";

    private final Context context;
    private final OkHttpClient client;
    private final OAuth2Service oauth2Service;
    private final TenantManager tenantManager;

    private WebSocket analyticsWebSocket;
    private WebSocket clusterWebSocket;
    private WebSocket systemWebSocket;

    private final Map<String, SyncCallback> callbacks = new HashMap<>();
    private boolean isConnected = false;

    public interface SyncCallback {
        void onDataReceived(String endpoint, JSONObject data);
        void onConnectionStatusChanged(String endpoint, boolean connected);
        void onError(String endpoint, String error);
    }

    public BackendSyncService(Context context, OAuth2Service oauth2Service, TenantManager tenantManager) {
        this.context = context;
        this.oauth2Service = oauth2Service;
        this.tenantManager = tenantManager;

        this.client = new OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .connectTimeout(30, TimeUnit.SECONDS)
                .pingInterval(30, TimeUnit.SECONDS)
                .build();
    }

    public void registerCallback(String endpoint, SyncCallback callback) {
        callbacks.put(endpoint, callback);
    }

    public void unregisterCallback(String endpoint) {
        callbacks.remove(endpoint);
    }

    public void connect() {
        if (isConnected) {
            Log.d(TAG, "Already connected to backend services");
            return;
        }

        Log.d(TAG, "Connecting to Docker backend services...");
        connectAnalytics();
        connectClusters();
        connectSystem();
        isConnected = true;
    }

    public void disconnect() {
        Log.d(TAG, "Disconnecting from backend services...");
        disconnectWebSocket(analyticsWebSocket, "analytics");
        disconnectWebSocket(clusterWebSocket, "clusters");
        disconnectWebSocket(systemWebSocket, "system");
        isConnected = false;
    }

    private void connectAnalytics() {
        String tenantId = tenantManager.getCurrentTenantId();
        String accessToken = oauth2Service.getAccessToken();

        Request request = new Request.Builder()
                .url(BACKEND_WS_URL + ANALYTICS_ENDPOINT + "?tenant=" + tenantId)
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

        analyticsWebSocket = client.newWebSocket(request, createWebSocketListener("analytics"));
    }

    private void connectClusters() {
        String tenantId = tenantManager.getCurrentTenantId();
        String accessToken = oauth2Service.getAccessToken();

        Request request = new Request.Builder()
                .url(BACKEND_WS_URL + CLUSTER_ENDPOINT + "?tenant=" + tenantId)
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

        clusterWebSocket = client.newWebSocket(request, createWebSocketListener("clusters"));
    }

    private void connectSystem() {
        String tenantId = tenantManager.getCurrentTenantId();
        String accessToken = oauth2Service.getAccessToken();

        Request request = new Request.Builder()
                .url(BACKEND_WS_URL + SYSTEM_ENDPOINT + "?tenant=" + tenantId)
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

        systemWebSocket = client.newWebSocket(request, createWebSocketListener("system"));
    }

    private WebSocketListener createWebSocketListener(String endpoint) {
        return new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, okhttp3.Response response) {
                Log.d(TAG, "Connected to " + endpoint + " WebSocket");
                SyncCallback callback = callbacks.get(endpoint);
                if (callback != null) {
                    callback.onConnectionStatusChanged(endpoint, true);
                }
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                try {
                    JSONObject json = new JSONObject(text);
                    Log.d(TAG, "Received message from " + endpoint + ": " + text);

                    SyncCallback callback = callbacks.get(endpoint);
                    if (callback != null) {
                        callback.onDataReceived(endpoint, json);
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Failed to parse JSON from " + endpoint, e);
                    SyncCallback callback = callbacks.get(endpoint);
                    if (callback != null) {
                        callback.onError(endpoint, "JSON parsing error: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                Log.d(TAG, "Received binary message from " + endpoint);
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                Log.d(TAG, "Closing " + endpoint + " WebSocket: " + reason);
                SyncCallback callback = callbacks.get(endpoint);
                if (callback != null) {
                    callback.onConnectionStatusChanged(endpoint, false);
                }
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, okhttp3.Response response) {
                Log.e(TAG, "WebSocket failure for " + endpoint, t);
                SyncCallback callback = callbacks.get(endpoint);
                if (callback != null) {
                    callback.onError(endpoint, t.getMessage());
                    callback.onConnectionStatusChanged(endpoint, false);
                }

                // Attempt reconnection after delay
                reconnectWebSocket(endpoint);
            }
        };
    }

    private void reconnectWebSocket(String endpoint) {
        Log.d(TAG, "Attempting to reconnect " + endpoint + " WebSocket in 5 seconds...");
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            if (isConnected) {
                switch (endpoint) {
                    case "analytics":
                        connectAnalytics();
                        break;
                    case "clusters":
                        connectClusters();
                        break;
                    case "system":
                        connectSystem();
                        break;
                }
            }
        }, 5000);
    }

    private void disconnectWebSocket(WebSocket webSocket, String endpoint) {
        if (webSocket != null) {
            webSocket.close(1000, "Service disconnecting");
            Log.d(TAG, "Disconnected " + endpoint + " WebSocket");
        }
    }

    public void sendMessage(String endpoint, String message) {
        WebSocket targetWebSocket = getWebSocketForEndpoint(endpoint);
        if (targetWebSocket != null) {
            targetWebSocket.send(message);
            Log.d(TAG, "Sent message to " + endpoint + ": " + message);
        } else {
            Log.w(TAG, "Cannot send message: " + endpoint + " WebSocket not connected");
        }
    }

    private WebSocket getWebSocketForEndpoint(String endpoint) {
        switch (endpoint) {
            case "analytics":
                return analyticsWebSocket;
            case "clusters":
                return clusterWebSocket;
            case "system":
                return systemWebSocket;
            default:
                return null;
        }
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void refreshConnections() {
        if (oauth2Service.isLoggedIn()) {
            Log.d(TAG, "Refreshing backend connections with new auth token");
            disconnect();
            connect();
        }
    }
}
