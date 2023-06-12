package com.example.slowchien.ui.contact;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
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
import com.example.slowchien.ui.location.JSONUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ContactFragment extends Fragment {
    private FragmentContactsBinding binding;
    private static final String CONTACT_FILE = "contact.json";


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ContactViewModel contactViewModel =
                new ViewModelProvider(this).get(ContactViewModel.class);

        binding = FragmentContactsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        initJSONFile();
        final TextView textBtnBT = binding.ButtonAddContact;
        contactViewModel.getContactBtnLib().observe(getViewLifecycleOwner(), textBtnBT::setText);

        Button mScanButton = root.findViewById(R.id.ButtonAddContact);

        mScanButton.setOnClickListener(v -> showAddContactDialog());

        return root;
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

            JSONUtils.ajouterValeurJSONContact(getContext(), CONTACT_FILE, macAddress, name, address, description);

            dialog.dismiss();
        });

        dialog.show();
    }




    public void initJSONFile(){

        try {
            // Récupération du fichier JSON contenu dans le répertoire assets
            InputStream inputStream = requireActivity().getAssets().open(CONTACT_FILE);
            String jsonString = new Scanner(inputStream).useDelimiter("\\A").next();

            // Copie du fichier JSON dans le stockage interne depuis le fichier assets
            JSONUtils.saveJsonFileToInternalStorage(getContext(), CONTACT_FILE, jsonString);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static List<JSONObject> getContactsFromJson(Context context, String fileName) {
        List<JSONObject> contactList = new ArrayList<>();

        try {
            InputStream inputStream = context.getAssets().open(fileName);
            String jsonString = new Scanner(inputStream).useDelimiter("\\A").next();

            JSONArray jsonArray = new JSONArray(jsonString);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                contactList.add(jsonObject);
            }

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return contactList;
    }




    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


}
