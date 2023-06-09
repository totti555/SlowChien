package com.example.slowchien.ui.home;

import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.slowchien.R;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<String> mText;
    //private final MutableLiveData<String> mTextBtnNewMessage;
    private final ListView mListView;

    public HomeViewModel(ListView listView, String[] countryList) {
        mText = new MutableLiveData<>();
        mListView = listView;
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(listView.getContext().getApplicationContext(), R.layout.activity_list_view, R.id.textView, countryList);
        mListView.setAdapter(arrayAdapter);
        mText.setValue("This is home fragment" + countryList);
        //mTextBtnNewMessage.setValue("+ Nouveau Message");
    }

    public LiveData<String> getText() {
        return mText;
    }
    //public LiveData<String> getNewMessageBtnLib() { return mTextBtnNewMessage; }
}