package com.uai.router.ui.ai;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.uai.router.R;
import com.uai.router.databinding.FragmentClaudeSonnetBinding;

public class ClaudeSonnetFragment extends Fragment {
    private FragmentClaudeSonnetBinding binding;
    private ClaudeSonnetViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ClaudeSonnetViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                            @Nullable Bundle savedInstanceState) {
        binding = FragmentClaudeSonnetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.sendButton.setOnClickListener(v -> {
            String prompt = binding.promptInput.getText().toString();
            if (!prompt.isEmpty()) {
                viewModel.sendPrompt(prompt);
            }
        });

        binding.usageButton.setOnClickListener(v -> viewModel.fetchUsageStats());

        viewModel.getResponse().observe(getViewLifecycleOwner(), response -> {
            binding.responseText.setText(response);
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.sendButton.setEnabled(!isLoading);
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                binding.errorText.setText(error);
                binding.errorText.setVisibility(View.VISIBLE);
            } else {
                binding.errorText.setVisibility(View.GONE);
            }
        });

        viewModel.getUsageStats().observe(getViewLifecycleOwner(), stats -> {
            if (stats != null) {
                String usageText = String.format(
                    "Total Tokens: %d\nPrompt Tokens: %d\nCompletion Tokens: %d\nTotal Cost: $%.2f",
                    stats.getTotalTokens(),
                    stats.getPromptTokens(),
                    stats.getCompletionTokens(),
                    stats.getTotalCost()
                );
                binding.usageText.setText(usageText);
                binding.usageText.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
