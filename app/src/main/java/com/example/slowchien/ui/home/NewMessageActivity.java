package com.example.slowchien.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.slowchien.R;
import com.example.slowchien.ui.contact.Contact;
import com.example.slowchien.ui.contact.ContactAdapter;
import com.example.slowchien.ui.location.JSONUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class NewMessageActivity extends AppCompatActivity {

    //private ListView listView;
    private List<Contact> contactList;
    private ContactAdapter adapter;

    private static final String JSON_DIRECTORY = "json";
    private static final String SENT_FILE = "contacts.json";

    public NewMessageActivity() {
        // Required empty public constructor
    }

    private void loadContactsFromJson() {
        // Charger les messages depuis le fichier JSON
        contactList = new ArrayList<>();

        try {
            File directory = new File(this.getFilesDir(), JSON_DIRECTORY);
            File file = new File(directory, SENT_FILE);
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
            //JSONUtils.sortMessagesByNewestDate(contactList,"Sent");
            //adapter = new ContactAdapter(this, contactList, "NewMessage");
            //listView.setAdapter(adapter);
        } catch (JSONException e ) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
    //protected void onCreate(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_message_view);

        Intent intent = getIntent();
        String pageName = intent.getStringExtra("pageName");

        getSupportActionBar().setTitle(pageName);

        //View view = inflater.inflate(R.layout.activity_new_message_view, container, false);
        //listView = view.findViewById(R.id.simpleListView);

        //ImageView icon = findViewById(R.id.mtrl_list_item_icon);
        //icon.setImageResource(R.drawable.ic_baseline_person_24);

        loadContactsFromJson();

        ListView listView = findViewById(R.id.simpleListView);

        adapter = new ContactAdapter(this, contactList, "newMessage");
        listView.setAdapter(adapter);

    }





/*
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }*/
}