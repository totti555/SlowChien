package com.example.slowchien.ui.home;

import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.slowchien.R;

import java.util.Arrays;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public HomeViewModel(ListView listView, String[] countryList) {
        mText = new MutableLiveData<>();
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(listView.getContext().getApplicationContext(), R.layout.activity_list_view, R.id.textView, countryList);
        listView.setAdapter(arrayAdapter);
        mText.setValue("This is home fragment" + Arrays.toString(countryList));
    }

    public LiveData<String> getText() {
        return mText;
    }
}