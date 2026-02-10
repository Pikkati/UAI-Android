package com.uairouter;

import android.content.Context;
import android.content.SharedPreferences;

public class TenantManager {
    private static final String PREFS_NAME = "tenant_prefs";
    private static final String TENANT_ID_KEY = "current_tenant_id";
    private static final String DEFAULT_TENANT_ID = "default";

    private final SharedPreferences prefs;

    public TenantManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public String getCurrentTenantId() {
        return prefs.getString(TENANT_ID_KEY, DEFAULT_TENANT_ID);
    }

    public void setCurrentTenantId(String tenantId) {
        prefs.edit().putString(TENANT_ID_KEY, tenantId).apply();
    }

    public boolean isDefaultTenant() {
        return DEFAULT_TENANT_ID.equals(getCurrentTenantId());
    }
}
