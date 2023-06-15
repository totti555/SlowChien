package com.example.slowchien.ui.exchange;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ExchangeViewModel extends ViewModel {
    private final MutableLiveData<String> mTextBtnBT;
    private final MutableLiveData<String> mTextBtnPairedDevices;
    private final MutableLiveData<String> mTextBtnVisibility;
    private final MutableLiveData<String> mTextBtnDisconnect;
    private final MutableLiveData<String> mTextBtnSending;

    public ExchangeViewModel() {

        mTextBtnBT = new MutableLiveData<>();
        mTextBtnBT.setValue("📡 Scan Bluetooth");

        mTextBtnPairedDevices = new MutableLiveData<>();
        mTextBtnPairedDevices.setValue("📳 Périphériques appairés");

        mTextBtnVisibility = new MutableLiveData<>();
        mTextBtnVisibility.setValue("👁 Se rendre visible");

        mTextBtnDisconnect = new MutableLiveData<>();
        mTextBtnDisconnect.setValue("❌ Déconnecter les appareils");

        mTextBtnSending = new MutableLiveData<>();
        mTextBtnSending.setValue("📤 Envoyer mes données");
    }

    public LiveData<String> getBluetoothBtnLib() { return mTextBtnBT; }

    public LiveData<String> getPairedDevicesBtnLib() { return mTextBtnPairedDevices; }

    public LiveData<String> getVisibilityBtnLib() { return mTextBtnVisibility; }

    public LiveData<String> getDisconnectBtnLib() { return mTextBtnDisconnect; }

    public LiveData<String> getSendingBtnLib() { return mTextBtnSending; }
}