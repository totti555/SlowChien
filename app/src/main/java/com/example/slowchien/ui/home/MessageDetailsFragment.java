package com.example.slowchien.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.slowchien.R;

public class MessageDetailsFragment extends Fragment {

    private Message mMessage;

    public MessageDetailsFragment() {
        // Constructeur vide requis par le FragmentManager
    }

    public MessageDetailsFragment(Message message) {
        mMessage = message;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_message_details, container, false);
    }

}
