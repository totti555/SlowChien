package com.example.slowchien.ui.settings;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SettingsViewModel extends ViewModel {
    private final MutableLiveData<String> mText;
    private final MutableLiveData<String> mTextBtnClean;

    public SettingsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is settings fragment");
        mTextBtnClean = new MutableLiveData<>();
        mTextBtnClean.setValue("Supprimer les données");
    }

    public LiveData<String> getText() {
        return mText;
    }
    public LiveData<String> getCleanBtnLib() { return mTextBtnClean; }
}
