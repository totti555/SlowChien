package com.example.slowchien.ui.home;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;

import com.example.slowchien.R;

import java.util.List;

public class ChatAdapter extends ArrayAdapter<Message> {

    private String myMacAddress = "FF-FF-FF-FF-FF-FF";

    public ChatAdapter(Context context, List<Message> messages) {
        super(context, 0, messages);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_chat_message, parent, false);
        }

        Message message = getItem(position);

        ConstraintLayout messageLayout = convertView.findViewById(R.id.messageLayout);
        TextView textMessage = convertView.findViewById(R.id.textMessage);
        TextView dateSentView = convertView.findViewById(R.id.textDate);
        LinearLayout messageOwnerLayout = convertView.findViewById(R.id.messageOwner);

        if (message.getMacAddressSrc().equals(myMacAddress)) {
            messageLayout.setBackgroundResource(R.color.blue);
            textMessage.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
            messageOwnerLayout.setGravity(Gravity.END);

            dateSentView.setText(message.getFormattedSentDate());
        } else {
            messageLayout.setBackgroundResource(R.color.gray);
            textMessage.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
            messageOwnerLayout.setGravity(Gravity.START);

            dateSentView.setText(message.getFormattedReceivedDate());
        }

        textMessage.setText(message.getName());

        return convertView;
    }
}