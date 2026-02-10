package com.uairouter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.ClientAuthentication;
import net.openid.appauth.ClientSecretBasic;
import net.openid.appauth.ResponseTypeValues;
import net.openid.appauth.TokenRequest;
import net.openid.appauth.TokenResponse;

import org.json.JSONException;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class OAuth2Service {
    private static final String TAG = "OAuth2Service";
    private static final String PREFS_NAME = "oauth_prefs";
    private static final String AUTH_STATE_KEY = "auth_state";

    private final Context context;
    private final AuthorizationService authService;
    private final SharedPreferences prefs;
    private AuthState authState;

    // Docker API Gateway OAuth2 configuration
    private static final String AUTH_ENDPOINT = "http://localhost:8080/oauth/authorize";
    private static final String TOKEN_ENDPOINT = "http://localhost:8080/oauth/token";
    private static final String CLIENT_ID = "uai_mobile_client";
    private static final String CLIENT_SECRET = "uai_mobile_secret_2024";
    private static final String REDIRECT_URI = "com.uairouter:/oauth2redirect";

    public OAuth2Service(Context context) {
        this.context = context;
        this.authService = new AuthorizationService(context);

        SharedPreferences tempPrefs = null;
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            tempPrefs = EncryptedSharedPreferences.create(
                    context,
                    PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            Log.e(TAG, "Failed to create encrypted preferences", e);
            // Fallback to regular SharedPreferences (less secure)
            tempPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        }
        this.prefs = tempPrefs;

        loadAuthState();
    }

    private void loadAuthState() {
        String authStateJson = prefs.getString(AUTH_STATE_KEY, null);
        if (authStateJson != null) {
            try {
                authState = AuthState.jsonDeserialize(authStateJson);
            } catch (JSONException e) {
                Log.e(TAG, "Failed to deserialize auth state", e);
                authState = new AuthState();
            }
        } else {
            authState = new AuthState();
        }
    }

    private void saveAuthState() {
        if (authState != null) {
            prefs.edit()
                    .putString(AUTH_STATE_KEY, authState.jsonSerializeString())
                    .apply();
        }
    }

    public boolean isLoggedIn() {
        return authState.isAuthorized() && !authState.getNeedsTokenRefresh();
    }

    public void performAuthorization() {
        AuthorizationServiceConfiguration serviceConfig = new AuthorizationServiceConfiguration(
                Uri.parse(AUTH_ENDPOINT),
                Uri.parse(TOKEN_ENDPOINT)
        );

        AuthorizationRequest.Builder authRequestBuilder = new AuthorizationRequest.Builder(
                serviceConfig,
                CLIENT_ID,
                ResponseTypeValues.CODE,
                Uri.parse(REDIRECT_URI)
        );

        AuthorizationRequest authRequest = authRequestBuilder
                .setScope("read write")
                .build();

        Intent authIntent = authService.getAuthorizationRequestIntent(authRequest);
        context.startActivity(authIntent);
    }

    public void handleAuthorizationResponse(Intent intent) {
        AuthorizationResponse response = AuthorizationResponse.fromIntent(intent);
        AuthorizationException ex = AuthorizationException.fromIntent(intent);

        authState.update(response, ex);

        if (response != null) {
            Log.d(TAG, "Authorization successful");
            performTokenRequest(response.createTokenExchangeRequest());
        } else {
            Log.e(TAG, "Authorization failed", ex);
        }
    }

    private void performTokenRequest(TokenRequest request) {
        ClientAuthentication clientAuth = new ClientSecretBasic(CLIENT_SECRET);

        authService.performTokenRequest(request, clientAuth, (response, ex) -> {
            authState.update(response, ex);

            if (response != null) {
                Log.d(TAG, "Token exchange successful");
                saveAuthState();
            } else {
                Log.e(TAG, "Token exchange failed", ex);
            }
        });
    }

    public void refreshAccessToken() {
        if (authState.getNeedsTokenRefresh()) {
            performTokenRequest(authState.createTokenRefreshRequest());
        }
    }

    public String getAccessToken() {
        return authState.getAccessToken();
    }

    public void logout() {
        authState = new AuthState();
        saveAuthState();
    }

    public void dispose() {
        authService.dispose();
    }
}
