package com.example.slowchien.ui.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.slowchien.R;

import java.util.List;

public class MessageAdapter extends ArrayAdapter<Message> {

    private String pageName;

    public MessageAdapter(Context context, List<Message> messages, String pageName) {
        super(context, 0, messages);
        this.pageName = pageName;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.activity_list_view, parent, false);
        }

        System.out.println("Message adapter chargement");

        Message message = getItem(position);

        TextView nameTextView = convertView.findViewById(R.id.nameView);
        nameTextView.setText(message.getName());

        TextView titleTextView = convertView.findViewById(R.id.textView);
        titleTextView.setText(message.getTitle());

        TextView macAddressView = convertView.findViewById(R.id.macAddressView);

        if (pageName.equals("Sent")) {
            macAddressView.setText(message.getMacAddressDest());
        } else {
            macAddressView.setText(message.getMacAddressSrc());
        }

        TextView dateSentView = convertView.findViewById(R.id.receivedDate);
        dateSentView.setText(message.getFormattedReceivedDate());

        TextView dateReceivedView = convertView.findViewById(R.id.sentDate);
        dateReceivedView.setText(message.getFormattedSentDate());

        ImageView icon = convertView.findViewById(R.id.mtrl_list_item_icon);
        icon.setImageResource(R.drawable.ic_email_24);

        return convertView;
    }
}