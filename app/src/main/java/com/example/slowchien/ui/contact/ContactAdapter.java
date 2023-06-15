package com.example.slowchien.ui.contact;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.slowchien.R;
import com.example.slowchien.ui.home.Message;

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
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.activity_list_view, parent, false);
        }


        Contact contact = getItem(position);

        TextView nameTextView = convertView.findViewById(R.id.nameView);
        nameTextView.setText(contact.getName());

        TextView macAddressView = convertView.findViewById(R.id.macAddressView);
        macAddressView.setText(contact.getMacAddress());

        ImageView icon = convertView.findViewById(R.id.mtrl_list_item_icon);
        icon.setImageResource(R.drawable.ic_baseline_person_24);

        return convertView;
    }
}