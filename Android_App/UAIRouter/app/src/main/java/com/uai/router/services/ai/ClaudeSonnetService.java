package com.uai.router.services.ai;

import com.uai.router.config.AppConfig;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.inject.Singleton;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;

import org.json.JSONObject;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;

@Singleton
public class ClaudeSonnetService {
    private static final String BASE_URL = "https://api.anthropic.com/v1";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient client;
    private final AppConfig appConfig;
    private String apiKey;

    @Inject
    public ClaudeSonnetService(AppConfig appConfig) {
        this.appConfig = appConfig;
        this.client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();
    }

    // Persist API key securely using EncryptedSharedPreferences
    public void persistApiKey(Context context, String apiKey) {
        try {
            SharedPreferences prefs = EncryptedPrefs.getEncryptedPrefs(context);
            prefs.edit().putString("claude_api_key", apiKey).apply();
            this.apiKey = apiKey;
        } catch (Exception e) {
            // fallback to plain shared prefs
            context.getSharedPreferences("uai_prefs", Context.MODE_PRIVATE).edit().putString("claude_api_key", apiKey).apply();
            this.apiKey = apiKey;
        }
    }

    public void initialize(String apiKey) {
        this.apiKey = apiKey;
    }

    public Single<ClaudeSonnetResponse> generateResponse(String prompt, ClaudeSonnetOptions options) {
        return Single.create(emitter -> {
            JSONObject requestBody = new JSONObject()
                .put("model", options.getModelVersion())
                .put("prompt", prompt)
                .put("max_tokens", options.getMaxTokens())
                .put("temperature", options.getTemperature())
                .put("top_p", options.getTopP())
                .put("stream", false);

            Request request = new Request.Builder()
                .url(BASE_URL + "/completions")
                .addHeader("x-api-key", apiKey)
                .addHeader("anthropic-version", "2024-10")
                .post(RequestBody.create(requestBody.toString(), JSON))
                .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    emitter.onError(new IOException("Unexpected response: " + response));
                    return;
                }

                JSONObject jsonResponse = new JSONObject(response.body().string());
                ClaudeSonnetResponse claudeResponse = new ClaudeSonnetResponse(
                    jsonResponse.getString("text"),
                    jsonResponse.getInt("tokens_used"),
                    jsonResponse.getDouble("cost")
                );
                emitter.onSuccess(claudeResponse);
            } catch (Exception e) {
                emitter.onError(e);
            }
        });
    }

    public Observable<String> streamResponse(String prompt, ClaudeSonnetOptions options) {
        PublishSubject<String> subject = PublishSubject.create();

        JSONObject requestBody = new JSONObject()
            .put("model", options.getModelVersion())
            .put("prompt", prompt)
            .put("max_tokens", options.getMaxTokens())
            .put("temperature", options.getTemperature())
            .put("top_p", options.getTopP())
            .put("stream", true);

        Request request = new Request.Builder()
            .url(BASE_URL + "/completions")
            .addHeader("x-api-key", apiKey)
            .addHeader("anthropic-version", "2024-10")
            .addHeader("accept", "text/event-stream")
            .post(RequestBody.create(requestBody.toString(), JSON))
            .build();

        EventSource.Factory factory = EventSources.createFactory(client);
        EventSource eventSource = factory.newEventSource(request, new EventSourceListener() {
            @Override
            public void onEvent(EventSource eventSource, String id, String type, String data) {
                if (data.startsWith("data: ")) {
                    String jsonData = data.substring(6);
                    try {
                        JSONObject jsonObject = new JSONObject(jsonData);
                        String text = jsonObject.getString("text");
                        if (!text.isEmpty()) {
                            subject.onNext(text);
                        }
                    } catch (Exception e) {
                        subject.onError(e);
                    }
                }
            }

            @Override
            public void onClosed(EventSource eventSource) {
                subject.onComplete();
            }

            @Override
            public void onFailure(EventSource eventSource, Throwable t, Response response) {
                subject.onError(t);
            }
        });

        return subject;
    }

    public Single<ClaudeSonnetUsageStats> getUsageStatistics() {
        return Single.create(emitter -> {
            Request request = new Request.Builder()
                .url(BASE_URL + "/usage")
                .addHeader("x-api-key", apiKey)
                .addHeader("anthropic-version", "2024-10")
                .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    emitter.onError(new IOException("Unexpected response: " + response));
                    return;
                }

                JSONObject jsonResponse = new JSONObject(response.body().string());
                ClaudeSonnetUsageStats stats = new ClaudeSonnetUsageStats(
                    jsonResponse.getLong("total_tokens"),
                    jsonResponse.getLong("prompt_tokens"),
                    jsonResponse.getLong("completion_tokens"),
                    jsonResponse.getDouble("total_cost")
                );
                // log analytics locally
                try {
                    // context may not be available here; attempt best-effort via appConfig
                    if (appConfig != null && appConfig.getContext() != null) {
                        AndroidAnalytics.logUsage(appConfig.getContext(), stats.getTotalTokens(), stats.getPromptTokens(), stats.getCompletionTokens(), stats.getTotalCost());
                    }
                } catch (Exception ex) {
                    // ignore
                }
                emitter.onSuccess(stats);
            } catch (Exception e) {
                emitter.onError(e);
            }
        });
    }
}
