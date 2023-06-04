package com.example.slowchien.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.slowchien.R;

import java.util.Objects;

public class MessageDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_message_details);

        Intent intent = getIntent();
        Message message = intent.getParcelableExtra("messageData");
        String pageName = intent.getStringExtra("pageName");

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(pageName);



        TextView name = findViewById(R.id.name);
        TextView macAddress = findViewById(R.id.macAddressView);
        TextView content = findViewById(R.id.textView);
        TextView receivedDate = findViewById(R.id.receivedDate);
        TextView sentDate = findViewById(R.id.sentDate);

        ImageView icon = findViewById(R.id.mtrl_list_item_icon);
        icon.setImageResource(R.drawable.ic_baseline_person_24);

        if (pageName.equals("Message envoyé")) {
            macAddress.setText(message.getMacAddressDest());
        } else {
            macAddress.setText(message.getMacAddressSrc());
        }

        name.setText(message.getName());
        receivedDate.setText(message.getFormattedReceivedDate());
        sentDate.setText(message.getFormattedSentDate());
        content.setText(message.getTitle());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}


