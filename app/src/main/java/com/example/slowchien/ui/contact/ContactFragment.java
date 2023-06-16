package com.example.slowchien.ui.contact;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.slowchien.R;
import com.example.slowchien.databinding.FragmentContactsBinding;
import com.example.slowchien.ui.home.MessageAdapter;
import com.example.slowchien.ui.location.JSONUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ContactFragment extends Fragment {
    private FragmentContactsBinding binding;
    private static final String JSON_DIRECTORY = "json";
    private static final String CONTACTS_FILE = "contacts.json";

    private Handler mHandler;
    private static final long REFRESH_INTERVAL = 5000; // 5 secondes
    private int lastVisibleItemPosition = 0;

    private List<Contact> contactList;
    private ContactAdapter contactAdapter;
    private  ListView mListView;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {



        //binding = FragmentContactsBinding.inflate(inflater, container, false);
        // View root = binding.getRoot();


        View view = inflater.inflate(R.layout.fragment_contacts, container, false);
        Button mScanButton = view.findViewById(R.id.ButtonAddContact);
        mListView = view.findViewById(R.id.simpleListView);
        loadContactsFromJson();
        mListView.setAdapter(contactAdapter);

        mScanButton.setOnClickListener(v -> showAddContactDialog());

        mHandler = new Handler();
        startRefreshing();
        return view;
    }

    private void showAddContactDialog() {
        Dialog dialog = new Dialog(requireContext(), R.style.RoundDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(getLayoutInflater().inflate(R.layout.fragement_contacts_popup_add,null));

        int width = getResources().getDisplayMetrics().widthPixels;
        int dialogWidth = (int) (width * 0.9); // 70% de la largeur de l'écran
        int dialogHeight = WindowManager.LayoutParams.WRAP_CONTENT;

        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.width = dialogWidth;
            params.height = dialogHeight;
            window.setAttributes(params);
            window.setGravity(Gravity.CENTER); // Aligner le dialogue au centre
        }

        // Ajouter du padding à gauche et à droite dans le contenu du dialogue
        View dialogContent = dialog.findViewById(R.id.dialog_content);
        int paddingStartEnd = (int) (width * 0.05); // 10% de la largeur de l'écran
        dialogContent.setPadding(paddingStartEnd, paddingStartEnd, paddingStartEnd, paddingStartEnd);

        EditText editTextMacAddress = dialog.findViewById(R.id.editTextMacAddress);
        EditText editTextName = dialog.findViewById(R.id.editTextName);
        EditText editTextAddress = dialog.findViewById(R.id.editTextAddress);
        EditText editTextDescription = dialog.findViewById(R.id.editTextDescription);
        Button buttonAddContact = dialog.findViewById(R.id.buttonAddContact);

        buttonAddContact.setOnClickListener(buttonView -> {
            String macAddress = editTextMacAddress.getText().toString();
            String name = editTextName.getText().toString();
            String address = editTextAddress.getText().toString();
            String description = editTextDescription.getText().toString();

            JSONUtils.ajouterValeurJSONContact(getContext(), CONTACTS_FILE, macAddress, name, address, description);

            dialog.dismiss();
        });

        dialog.show();
    }


    public void loadContactsFromJson() {
        contactList = new ArrayList<>();

        try {
            File directory = new File(requireContext().getFilesDir(), JSON_DIRECTORY);
            File file = new File(directory, CONTACTS_FILE);

            String jsonString = JSONUtils.loadJSONFromFile(file.getAbsolutePath());
            JSONArray jsonArray = new JSONArray(jsonString);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                System.out.println(jsonObject.toString());
                String name = jsonObject.getString("name");
                String address = jsonObject.getString("address");
                String description = jsonObject.getString("description");
                String macAddress = jsonObject.getString("macAddress");
                contactList.add(new Contact(name,address,description,macAddress));
            }
            contactAdapter = new ContactAdapter(getActivity(), contactList, "Sent");

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void startRefreshing() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshData();
                // Planifier le prochain rafraîchissement après l'intervalle défini
                mHandler.postDelayed(this, REFRESH_INTERVAL);
            }
        }, REFRESH_INTERVAL);
    }

    private void refreshData() {
        // Scroll position and reload
        lastVisibleItemPosition = mListView.getFirstVisiblePosition();
        loadContactsFromJson();
        mListView.setAdapter(contactAdapter);
        mListView.setSelection(lastVisibleItemPosition);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mHandler.removeCallbacksAndMessages(null);
        binding = null;
    }


}
