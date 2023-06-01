package com.example.slowchien.ui.home;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.slowchien.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity {

    String myMacAddress = "FF-FF-FF-FF-FF-FF";
    private EditText messageEditText;
    private ImageButton sendButton;
    private String selectedName;
    private String selectedMacAddress;
    private List<Message> messagesWithSameMacAddress;
    private ChatAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_view);

        selectedMacAddress = getIntent().getStringExtra("selectedMacAddress");
        selectedName = getIntent().getStringExtra("name");

        messagesWithSameMacAddress = new ArrayList<>();

        String filePath = getApplicationContext().getFilesDir().getPath() + "/chat.json";

        try {
            FileInputStream fileInputStream = new FileInputStream(filePath);
            StringBuilder stringBuilder = new StringBuilder();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            String jsonString = stringBuilder.toString();
            bufferedReader.close();
            fileInputStream.close();

            JSONArray jsonArray = new JSONArray(jsonString);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String macAddressSrc = jsonObject.getString("macAddressSrc");
                String macAddressDest = jsonObject.getString("macAddressDest");
                if (macAddressSrc.equals(selectedMacAddress) || macAddressDest.equals(selectedMacAddress)) {
                    String sentDateStr = jsonObject.getString("sentDate");
                    String receiveDateStr = jsonObject.getString("receivedDate");
                    String content = jsonObject.getString("content");

                    SimpleDateFormat dateFormat;
                    if (sentDateStr.contains("GMT")) {
                        dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss 'GMT' yyyy", Locale.ENGLISH);
                    } else if (sentDateStr.contains(":")) {
                        dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                    } else {
                        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    }

                    Date sentDate = null;
                    Date receiveDate = null;
                    try {
                        sentDate = dateFormat.parse(sentDateStr);
                        receiveDate = dateFormat.parse(receiveDateStr);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    Message message = new Message(receiveDate, sentDate, content, macAddressSrc, macAddressDest);
                    messagesWithSameMacAddress.add(message);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        messageEditText = findViewById(R.id.messageEditText);

        sendButton = findViewById(R.id.sendButton);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageContent = messageEditText.getText().toString();
                try {
                    sendMessage(messageContent, filePath);
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        sendButton.setEnabled(false); // Désactiver le bouton d'envoi par défaut

        messageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    sendButton.setEnabled(true); // Activer le bouton d'envoi lorsque du texte est saisi

                    // Changer la couleur de l'icône en violet
                    Drawable icon = getResources().getDrawable(R.drawable.ic_send_24);
                    icon.setColorFilter(getResources().getColor(R.color.blue), PorterDuff.Mode.SRC_IN);
                    sendButton.setImageDrawable(icon);
                } else {
                    sendButton.setEnabled(false); // Désactiver le bouton d'envoi lorsque le champ est vide

                    // Rétablir la couleur de l'icône par défaut
                    Drawable icon = getResources().getDrawable(R.drawable.ic_send_24);
                    sendButton.setImageDrawable(icon);
                }
            }
        });

        TextView textMacAddress = findViewById(R.id.textMacAddress);
        textMacAddress.setText(selectedMacAddress);

        TextView textView = findViewById(R.id.textName);
        textView.setText(selectedName);

        ImageView icon = findViewById(R.id.mtrl_list_item_icon);
        icon.setImageResource(R.drawable.ic_baseline_person_24);

        ListView listView = findViewById(R.id.simpleListView);
        listView.setDividerHeight(4);
        listView.setDivider(null);

        adapter = new ChatAdapter(this, messagesWithSameMacAddress);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void sendMessage(String messageContent, String filePath) throws IOException, JSONException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String formattedDate = dateFormat.format(new Date());

        Date currentDate = null;
        try {
            currentDate = dateFormat.parse(formattedDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Message newMessage = new Message(selectedName, currentDate, currentDate, messageContent, myMacAddress, selectedMacAddress);

        JSONArray jsonArray = loadJSONArrayFromFile(filePath);
        JSONObject messageObject = new JSONObject();
        messageObject.put("macAddressSrc", newMessage.getMacAddressSrc());
        messageObject.put("macAddressDest", newMessage.getMacAddressDest());
        messageObject.put("sentDate", newMessage.getFormattedSentDate());
        messageObject.put("receivedDate", newMessage.getFormattedReceivedDate());
        messageObject.put("content", newMessage.getName());
        jsonArray.put(messageObject);

        FileWriter fileWriter = new FileWriter(filePath);
        fileWriter.write(jsonArray.toString());
        fileWriter.flush();
        fileWriter.close();

        messagesWithSameMacAddress.add(newMessage);

        messageEditText.setText("");
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(messageEditText.getWindowToken(), 0);

        // Recharge la page
        recreate();
    }

    private JSONArray loadJSONArrayFromFile(String filePath) throws IOException, JSONException {
        FileInputStream fileInputStream = new FileInputStream(filePath);
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line);
        }
        String jsonString = stringBuilder.toString();
        bufferedReader.close();
        fileInputStream.close();
        return new JSONArray(jsonString);
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
