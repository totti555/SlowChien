package com.example.slowchien.ui.contact;

import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.slowchien.R;

import org.json.JSONObject;

import java.util.List;

public class ContactViewModel extends ViewModel {
    private final MutableLiveData<String> mText;
    private final MutableLiveData<String> mTextBtnCtct;
    private final MutableLiveData<ListView> mListView;

    public ContactViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is contact fragment");

        mTextBtnCtct = new MutableLiveData<>();
        mTextBtnCtct.setValue("+ Ajouter un contact");

        mListView = new MutableLiveData<>();
    }

    public LiveData<String> getText() {
        return mText;
    }
    public LiveData<String> getContactBtnLib() { return mTextBtnCtct; }
}
