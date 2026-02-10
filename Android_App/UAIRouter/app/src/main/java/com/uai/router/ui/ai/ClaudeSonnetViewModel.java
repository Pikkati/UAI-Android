package com.uai.router.ui.ai;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.uai.router.services.ai.ClaudeSonnetOptions;
import com.uai.router.services.ai.ClaudeSonnetService;
import com.uai.router.services.ai.ClaudeSonnetUsageStats;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class ClaudeSonnetViewModel extends ViewModel {
    private final ClaudeSonnetService claudeService;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private final MutableLiveData<String> response = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<ClaudeSonnetUsageStats> usageStats = new MutableLiveData<>();

    @Inject
    public ClaudeSonnetViewModel(ClaudeSonnetService claudeService) {
        this.claudeService = claudeService;
    }

    public void sendPrompt(String prompt) {
        isLoading.setValue(true);
        error.setValue(null);

        ClaudeSonnetOptions options = new ClaudeSonnetOptions.Builder()
            .modelVersion("sonnet-3.5")
            .maxTokens(4096)
            .temperature(0.7f)
            .build();

        disposables.add(
            claudeService.streamResponse(prompt, options)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete(() -> isLoading.setValue(false))
                .doOnError(throwable -> {
                    error.setValue(throwable.getMessage());
                    isLoading.setValue(false);
                })
                .subscribe(
                    text -> {
                        String currentResponse = response.getValue();
                        response.setValue(currentResponse != null ? currentResponse + text : text);
                    },
                    throwable -> error.setValue(throwable.getMessage())
                )
        );
    }

    public void fetchUsageStats() {
        isLoading.setValue(true);
        error.setValue(null);

        disposables.add(
            claudeService.getUsageStatistics()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(() -> isLoading.setValue(false))
                .subscribe(
                    stats -> usageStats.setValue(stats),
                    throwable -> error.setValue(throwable.getMessage())
                )
        );
    }

    public LiveData<String> getResponse() {
        return response;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<ClaudeSonnetUsageStats> getUsageStats() {
        return usageStats;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }
}
