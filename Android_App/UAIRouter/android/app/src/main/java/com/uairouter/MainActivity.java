package com.uairouter;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private LinearLayout mainLayout;
    private TextView authStatusText;
    private Button loginBtn;
    private Button logoutBtn;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    private OAuth2Service oauth2Service;
    private TenantManager tenantManager;
    private Spinner tenantSpinner;
    private BackendSyncService backendSyncService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate: MainActivity initialized");

        oauth2Service = new OAuth2Service(this);
        tenantManager = new TenantManager(this);

        mainLayout = findViewById(R.id.main_layout);
        authStatusText = findViewById(R.id.auth_status);
        loginBtn = findViewById(R.id.btn_login);
        logoutBtn = findViewById(R.id.btn_logout);
        tenantSpinner = findViewById(R.id.tenant_spinner);

        // Setup tenant spinner
        String currentTenant = tenantManager.getCurrentTenantId();
        String[] tenantOptions = getResources().getStringArray(R.array.tenant_options);
        int currentPosition = getTenantPosition(currentTenant, tenantOptions);
        tenantSpinner.setSelection(currentPosition);
        tenantSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedTenant = getTenantIdFromPosition(position, tenantOptions);
                tenantManager.setCurrentTenantId(selectedTenant);

                // Refresh backend connections when tenant changes
                if (backendSyncService != null && oauth2Service.isLoggedIn()) {
                    backendSyncService.refreshConnections();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Initialize backend sync service
        backendSyncService = new BackendSyncService(this, oauth2Service, tenantManager);

        // Setup OAuth2 buttons
        if (loginBtn != null) {
            loginBtn.setOnClickListener(v -> oauth2Service.performAuthorization());
        }

        if (logoutBtn != null) {
            logoutBtn.setOnClickListener(v -> {
                oauth2Service.logout();
                updateAuthUI();
            });
        }

        updateAuthUI();
        setupButtons();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        // Handle OAuth2 redirect
        if (intent.getData() != null && intent.getData().toString().startsWith("com.uairouter")) {
            oauth2Service.handleAuthorizationResponse(intent);
            updateAuthUI();
        }
    }

    private void updateAuthUI() {
        if (oauth2Service.isLoggedIn()) {
            authStatusText.setText("Logged in to UAI Router");
            loginBtn.setVisibility(View.GONE);
            logoutBtn.setVisibility(View.VISIBLE);

            // Connect to backend services
            if (backendSyncService != null) {
                backendSyncService.connect();
            }

            // Check biometric availability after OAuth2 login
            checkBiometricAndShow();
        } else {
            authStatusText.setText("Please log in to UAI Router");
            loginBtn.setVisibility(View.VISIBLE);
            logoutBtn.setVisibility(View.GONE);
            mainLayout.setVisibility(View.GONE);

            // Disconnect from backend services
            if (backendSyncService != null) {
                backendSyncService.disconnect();
            }
        }
    }

    private void checkBiometricAndShow() {
        BiometricManager biometricManager = BiometricManager.from(this);
        switch (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                Log.d(TAG, "Biometric authentication available");
                setupBiometricAuth();
                showAuthPrompt();
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Log.d(TAG, "No biometric hardware available");
                showMainInterface();
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Log.d(TAG, "Biometric hardware unavailable");
                showMainInterface();
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                Log.d(TAG, "No biometric credentials enrolled");
                showMainInterface();
                break;
        }
    }

    private void setupBiometricAuth() {
        Executor executor = ContextCompat.getMainExecutor(this);

        biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Log.d(TAG, "Biometric authentication succeeded");
                runOnUiThread(() -> showMainInterface());
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Log.d(TAG, "Biometric authentication failed");
                runOnUiThread(() -> {
                    authStatusText.setText("Biometric authentication failed. Please try again.");
                    showAuthPrompt();
                });
            }

            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Log.d(TAG, "Biometric authentication error: " + errString);
                runOnUiThread(() -> {
                    authStatusText.setText("Biometric authentication error: " + errString);
                    // Show main interface anyway for demo purposes
                    showMainInterface();
                });
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("UAI Router Authentication")
                .setSubtitle("Authenticate to access the application")
                .setDescription("Use your fingerprint or face to unlock the UAI Router")
                .setNegativeButtonText("Cancel")
                .build();
    }

    private void showAuthPrompt() {
        if (biometricPrompt != null) {
            mainLayout.setVisibility(View.GONE);
            authStatusText.setVisibility(View.VISIBLE);
            authStatusText.setText("Authenticating...");
            biometricPrompt.authenticate(promptInfo);
        }
    }

    private void showMainInterface() {
        mainLayout.setVisibility(View.VISIBLE);
        authStatusText.setVisibility(View.GONE);
    }

    private void setupButtons() {
        Button systemDetailsBtn = findViewById(R.id.btn_system_details);
        Button webDemoBtn = findViewById(R.id.btn_web_demo);
        Button analyticsBtn = findViewById(R.id.btn_analytics);
        Button clusterVizBtn = findViewById(R.id.btn_cluster_viz);

        if (systemDetailsBtn != null) {
            systemDetailsBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, SystemDetailsActivity.class);
                    startActivity(intent);
                }
            });
        }

        if (webDemoBtn != null) {
            webDemoBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, WebDemoActivity.class);
                    startActivity(intent);
                }
            });
        }

        if (analyticsBtn != null) {
            analyticsBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, AnalyticsActivity.class);
                    startActivity(intent);
                }
            });
        }

        if (clusterVizBtn != null) {
            clusterVizBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Launching ClusterVisualizationActivity");
                    Intent intent = new Intent(MainActivity.this, ClusterVisualizationActivity.class);
                    startActivity(intent);
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (oauth2Service != null) {
            oauth2Service.dispose();
        }
    }

    private int getTenantPosition(String tenantId, String[] tenantOptions) {
        switch (tenantId) {
            case "default": return 0;
            case "enterprise": return 1;
            case "development": return 2;
            case "production": return 3;
            default: return 0;
        }
    }

    private String getTenantIdFromPosition(int position, String[] tenantOptions) {
        switch (position) {
            case 0: return "default";
            case 1: return "enterprise";
            case 2: return "development";
            case 3: return "production";
            default: return "default";
        }
    }
}
