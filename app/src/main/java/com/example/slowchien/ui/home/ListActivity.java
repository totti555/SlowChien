package com.example.slowchien.ui.home;

import android.os.Bundle;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.slowchien.R;

public class ListActivity extends AppCompatActivity {

    private ListView mListView;
    private String[] countryList;
    private HomeViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_sent);

        mListView = findViewById(R.id.simpleListView);

        mViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        mViewModel = new HomeViewModel(mListView,countryList);

        mViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                // Update your UI here based on the LiveData
            }
        });
    }
}
