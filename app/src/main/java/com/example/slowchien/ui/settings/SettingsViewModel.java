package com.example.slowchien.ui.settings;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.slowchien.MainActivity;

public class SettingsViewModel extends ViewModel {
    private final MutableLiveData<String> mTextBtnClean;
    private final MutableLiveData<String> mTextBtnChangeMACAdress;
    private final MutableLiveData<String> mTextMACAddress;

    public SettingsViewModel() {
        mTextBtnClean = new MutableLiveData<>();
        mTextBtnClean.setValue("❌ Supprimer les données");

        mTextBtnChangeMACAdress = new MutableLiveData<>();
        mTextBtnChangeMACAdress.setValue("🛠 Modifier adresse MAC");

        mTextMACAddress = new MutableLiveData<>();
        mTextMACAddress.setValue("Adresse MAC\n"+ MainActivity.getMacAddr());
    }

    public LiveData<String> getCleanBtnLib() { return mTextBtnClean; }
    public LiveData<String> getChangeMACAdress() { return mTextBtnChangeMACAdress; }
    public LiveData<String> getMACAddressText() {return mTextMACAddress;}
}
