package com.example.slowchien.ui.contact;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.slowchien.R;
import com.example.slowchien.ui.home.Message;
import com.example.slowchien.ui.location.JSONUtils;

import java.util.List;

public class ContactAdapter extends ArrayAdapter<Contact> {

    private String pageName;

    public ContactAdapter(Context context, List<Contact> contacts, String pageName) {
        super(context, 0, contacts);
        this.pageName = pageName;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.activity_contacts_list_view, parent, false);
        }


        Contact contact = getItem(position);

        TextView nameTextView = convertView.findViewById(R.id.nameView);
        nameTextView.setText(contact.getName());

        TextView macAddressView = convertView.findViewById(R.id.macAddressView);
        macAddressView.setText(contact.getMacAddress());

        TextView descriptionView = convertView.findViewById(R.id.textView);
        descriptionView.setText(contact.getDescription());

        ImageView icon = convertView.findViewById(R.id.mtrl_list_item_icon);
        icon.setImageResource(R.drawable.ic_baseline_person_24);

        Button deleteButton = convertView.findViewById(R.id.deleleContact);
        deleteButton.setOnClickListener(v -> showDeletePopup(contact));

        return convertView;
    }
    private void showDeletePopup(Contact contact) {

        System.out.println("click est fait !");
        System.out.println("voulez vous supprmier " + contact.getName());
        JSONUtils.deleteOneContact(super.getContext(),contact);
        /*
        Dialog dialog = new Dialog(requireContext(), R.style.RoundDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(getLayoutInflater().inflate(R.layout.fragment_contacts_popup_add,null));

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

            JSONUtils.ajouterValeurJSONContact(requireContext(), CONTACTS_FILE, macAddress, name, address, description);

            dialog.dismiss();
        });

        dialog.show();*/
    }
}