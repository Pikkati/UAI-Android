package com.uairouter;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;

public class WebDemoActivity extends Activity {
    private WebView webView;
    private boolean isLocal = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_demo);

        webView = findViewById(R.id.webview);
        Button toggleBtn = findViewById(R.id.btn_toggle);
        EditText urlInput = findViewById(R.id.url_input);
        Button loadBtn = findViewById(R.id.btn_load_url);

        if (webView != null) {
            WebSettings settings = webView.getSettings();
            settings.setJavaScriptEnabled(true);
            settings.setDomStorageEnabled(true);
            webView.setWebViewClient(new WebViewClient());
            loadLocal();
        }

        if (toggleBtn != null) {
            toggleBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isLocal) {
                        loadRemote();
                        toggleBtn.setText(R.string.load_local_demo);
                    } else {
                        loadLocal();
                        toggleBtn.setText(R.string.load_remote_demo);
                    }
                    isLocal = !isLocal;
                }
            });
        }

        if (loadBtn != null && urlInput != null) {
            loadBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = urlInput.getText().toString().trim();
                    if (!url.isEmpty()) {
                        if (!url.startsWith("http://") && !url.startsWith("https://")) {
                            url = "https://" + url;
                        }
                        webView.loadUrl(url);
                    }
                }
            });
        }
    }

    private void loadLocal() {
        webView.loadUrl("file:///android_asset/demo.html");
    }

    private void loadRemote() {
        webView.loadUrl("https://www.google.com");
    }
}
