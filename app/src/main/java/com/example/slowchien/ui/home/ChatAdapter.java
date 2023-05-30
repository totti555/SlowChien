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

public class ChatAdapter extends ArrayAdapter<Message> {

    public ChatAdapter(Context context, List<Message> messages) {
        super(context, 0, messages);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_chat_message, parent, false);
        }

        Message message = getItem(position);
        System.out.println(message);

        TextView titleTextView = convertView.findViewById(R.id.textContent);
        System.out.println(message.getName());
        titleTextView.setText(message.getName());

        //TextView dateSentView = convertView.findViewById(R.id.textSentDate);
        // dateSentView.setText(message.getFormattedReceivedDate());

        // TextView dateReceivedView = convertView.findViewById(R.id.textReceiveDate);
        // dateReceivedView.setText(message.getFormattedSentDate());

        //ImageView icon = convertView.findViewById(R.id.mtrl_list_item_icon);
        // icon.setImageResource(R.drawable.ic_email_24);

        return convertView;
    }
}