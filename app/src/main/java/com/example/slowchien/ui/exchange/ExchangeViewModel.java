package com.example.slowchien.ui.exchange;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ExchangeViewModel extends ViewModel {
    private final MutableLiveData<String> mText;
    private final MutableLiveData<String> mTextBtnBT;

    public ExchangeViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is exchange fragment");

        mTextBtnBT = new MutableLiveData<>();
        mTextBtnBT.setValue("📡 Scan Bluetooth");
    }

    public LiveData<String> getText() {
        return mText;
    }

    public LiveData<String> getBluetoothBtnLib() { return mTextBtnBT; }
}