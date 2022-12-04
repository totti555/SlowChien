package com.example.slowchien.ui.exchange;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.slowchien.databinding.FragmentExchangeBinding;

public class ExchangeFragment extends Fragment {

    private FragmentExchangeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ExchangeViewModel exchangeViewModel =
                new ViewModelProvider(this).get(ExchangeViewModel.class);

        binding = FragmentExchangeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        //  Match to the id of the exchange layout
        final TextView textView = binding.textExchange;
        exchangeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}