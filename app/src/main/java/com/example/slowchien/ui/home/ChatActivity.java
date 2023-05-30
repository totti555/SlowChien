package com.example.slowchien.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.slowchien.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class ChatActivity extends AppCompatActivity {

String myMacAddress = "FF-FF-FF-FF-FF-FF";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_view);

        String selectedMacAddress = getIntent().getStringExtra("selectedMacAddress");
        String selectedName = getIntent().getStringExtra("name");

        // Filtrer les données de la liste en fonction de la macAddress
        List<Message> messagesWithSameMacAddress = new ArrayList<>();

        try {
            InputStream inputStream = getAssets().open("chat.json");
            String jsonString = new Scanner(inputStream).useDelimiter("\\A").next();
            JSONArray jsonArray = new JSONArray(jsonString);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String macAddressSrc = jsonObject.getString("macAddressSrc");
                String macAddressDest = jsonObject.getString("macAddressDest");
                if (macAddressSrc.equals(selectedMacAddress) || macAddressDest.equals(selectedMacAddress)  ) {
                    String sentDateStr = jsonObject.getString("sentDate");
                    String receiveDateStr = jsonObject.getString("receivedDate");
                    String content = jsonObject.getString("content");

                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

                    Date sentDate = null;
                    Date receiveDate = null;
                    try {
                        sentDate = dateFormat.parse(sentDateStr);
                        receiveDate = dateFormat.parse(receiveDateStr);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    Message message = new Message(sentDate, receiveDate, content);
                    messagesWithSameMacAddress.add(message);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }



        TextView textMacAddress = findViewById(R.id.textMacAddress);
        textMacAddress.setText(selectedMacAddress);

        TextView textView = findViewById(R.id.textName);
        textView.setText(selectedName);



        ListView listView = findViewById(R.id.simpleListView);
        System.out.println("SALUT");

        ChatAdapter adapter = new ChatAdapter(this, messagesWithSameMacAddress);
        listView.setAdapter(adapter);

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