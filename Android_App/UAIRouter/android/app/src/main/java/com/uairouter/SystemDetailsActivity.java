package com.uairouter;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;

public class SystemDetailsActivity extends Activity {
    private BroadcastReceiver batteryReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_details);

        TextView tv = findViewById(R.id.system_info);
        String info = getString(R.string.system_details) + "\n\n" +
                "Manufacturer: " + Build.MANUFACTURER +
                "\nModel: " + Build.MODEL +
                "\nDevice: " + Build.DEVICE +
                "\nProduct: " + Build.PRODUCT +
                "\nBrand: " + Build.BRAND +
                "\nAndroid: " + Build.VERSION.RELEASE + " (SDK " + Build.VERSION.SDK_INT + ")";

        // Network info
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        String networkInfo = "\n\nNetwork: ";
        if (activeNetwork != null && activeNetwork.isConnected()) {
            networkInfo += "Connected (" + activeNetwork.getTypeName() + ")";
        } else {
            networkInfo += "Not connected";
        }

        tv.setText(info + networkInfo);

        // Battery info
        batteryReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                                     status == BatteryManager.BATTERY_STATUS_FULL;

                float batteryPct = level / (float) scale * 100;
                String batteryInfo = String.format("\n\nBattery: %.0f%% (%s)", batteryPct, isCharging ? "Charging" : "Not Charging");
                tv.append(batteryInfo);
            }
        };
        registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (batteryReceiver != null) {
            unregisterReceiver(batteryReceiver);
        }
    }
}
