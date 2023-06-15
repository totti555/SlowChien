package com.example.slowchien.ui.contact;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    private List<Contact> contactList;
    private ContactAdapter contactAdapter;
    private  ListView mListView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ContactViewModel contactViewModel =
                new ViewModelProvider(this).get(ContactViewModel.class);


        binding = FragmentContactsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textBtnBT = binding.ButtonAddContact;
        contactViewModel.getContactBtnLib().observe(getViewLifecycleOwner(), textBtnBT::setText);

        Button mScanButton = root.findViewById(R.id.ButtonAddContact);


        View view = inflater.inflate(R.layout.fragment_contacts, container, false);
        mListView = view.findViewById(R.id.simpleListView);
        loadContactsFromJson();
        mListView.setAdapter(contactAdapter);

        // Ajout d'un écouteur sur le bouton de scan
            mScanButton.setOnClickListener(v -> {

                // Créer une instance de PopupWindow
                PopupWindow popupWindow = new PopupWindow(getActivity());
                // Définir la focusabilité de la popup
                popupWindow.setFocusable(true);
                // Permettre à la popup de recevoir les événements tactiles en dehors de sa zone
                popupWindow.setOutsideTouchable(false);
                // Charger le contenu de la popup à partir du fichier XML
                View popupView = getLayoutInflater().inflate(R.layout.fragement_contacts_popup_add, null);
                popupWindow.setContentView(popupView);
                // Définir la largeur et la hauteur de la popup
                popupWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
                popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
                // Afficher la popup au centre de l'écran
                popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);

                // Récupérer les références des champs de saisie dans la popup
                EditText editTextMacAddress = popupView.findViewById(R.id.editTextMacAddress);
                EditText editTextName = popupView.findViewById(R.id.editTextName);
                EditText editTextAddress = popupView.findViewById(R.id.editTextAddress);
                EditText editTextDescription = popupView.findViewById(R.id.editTextDescription);
                Button buttonAddContact = popupView.findViewById(R.id.buttonAddContact);

                // Écouter l'appui sur le bouton "Ajouter" dans la popup
                buttonAddContact.setOnClickListener(buttonView -> {
                    // Récupérer les valeurs saisies dans les champs
                    String macAddress = editTextMacAddress.getText().toString();
                    String name = editTextName.getText().toString();
                    String address = editTextAddress.getText().toString();
                    String description = editTextDescription.getText().toString();

                    JSONUtils.ajouterValeurJSONContact(getContext(), CONTACTS_FILE,macAddress, name,address,description);

                    // Fermer la popup
                    popupWindow.dismiss();
                });
            });


        return root;
    }





    public void loadContactsFromJson() {
        List<Contact> contactList = new ArrayList<>();

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
            //contactAdapter = new ContactAdapter(getActivity(), contactList, "Contacts");
            if (contactAdapter == null) {
                contactAdapter = new ContactAdapter(getActivity(), contactList, "Contacts");
            } else {
                contactAdapter.notifyDataSetChanged();
            }
            mListView.setAdapter(contactAdapter);

        } catch ( JSONException e) {
            e.printStackTrace();
        }

        this.contactList = contactList;
        System.out.println("test " );
        System.out.println(contactList);
    }




    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


}
