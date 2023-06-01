package com.example.slowchien.ui.home;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.TimeZone;

public class ChatActivity extends AppCompatActivity {

    String myMacAddress = "FF-FF-FF-FF-FF-FF";
    private EditText messageEditText;
    private Button sendButton;
    private String selectedName;
    private String selectedMacAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_view);

        selectedMacAddress = getIntent().getStringExtra("selectedMacAddress");
        selectedName = getIntent().getStringExtra("name");

        // Filtrer les données de la liste en fonction de la macAddress
        List<Message> messagesWithSameMacAddress = new ArrayList<>();

        String filePath = getApplicationContext().getFilesDir().getPath() + "/chat.json";


        try {

            // Créer un FileInputStream pour lire le fichier

            FileInputStream fileInputStream = new FileInputStream(filePath);

            // Lire le contenu du fichier en tant que chaîne de caractères
            StringBuilder stringBuilder = new StringBuilder();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            String jsonString = stringBuilder.toString();

            // Convertir la chaîne JSON en un objet JSONArray
            JSONArray jsonArray = new JSONArray(jsonString);

            // Fermer le flux de lecture
            bufferedReader.close();
            fileInputStream.close();


            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String macAddressSrc = jsonObject.getString("macAddressSrc");
                String macAddressDest = jsonObject.getString("macAddressDest");
                if (macAddressSrc.equals(selectedMacAddress) || macAddressDest.equals(selectedMacAddress)) {
                    String sentDateStr = jsonObject.getString("sentDate");
                    String receiveDateStr = jsonObject.getString("receivedDate");
                    String content = jsonObject.getString("content");
                    System.out.println(receiveDateStr);
                    SimpleDateFormat dateFormat;
                     if (sentDateStr.contains("GMT")) {
                        dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss 'GMT' yyyy", Locale.ENGLISH);
                    }
                    else if (sentDateStr.contains(":")) {
                        dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                    }

                     else {
                        dateFormat = new SimpleDateFormat("yyyy-MM-dd");
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
                    List<Message> sortedMessages = new ArrayList<>(messagesWithSameMacAddress);
                    Collections.sort(messagesWithSameMacAddress, new Comparator<Message>() {
                        @Override
                        public int compare(Message m1, Message m2) {
                            Date date1 = m1.getReceivedDate();
                            Date date2 = m2.getReceivedDate();
                            return date1.compareTo(date2);
                        }
                    });
                    System.out.println(messagesWithSameMacAddress);

                    // Remove the last member from the JSONArray
                    jsonArray.remove(i);
                    i--; // Adjust the index to avoid skipping the next element
                }
            }

            messageEditText = findViewById(R.id.messageEditText);

            sendButton = findViewById(R.id.sendButton);
            sendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String messageContent = messageEditText.getText().toString();
                    System.out.println("COUCOU");
                    try {
                        sendMessage(messageContent);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

        TextView textMacAddress = findViewById(R.id.textMacAddress);
        textMacAddress.setText(selectedMacAddress);

        TextView textView = findViewById(R.id.textName);
        textView.setText(selectedName);

        ImageView icon = findViewById(R.id.mtrl_list_item_icon);
        icon.setImageResource(R.drawable.ic_baseline_person_24);

        ListView listView = findViewById(R.id.simpleListView);
        listView.setDividerHeight(4);
        listView.setDivider(null);

        System.out.println("SALUT");

        ChatAdapter adapter = new ChatAdapter(this, messagesWithSameMacAddress);
        listView.setAdapter(adapter);
    }

    private void sendMessage(String messageContent) throws IOException, JSONException {
        // Créer un objet Message représentant le message à envoyer
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String formattedDate = dateFormat.format(new Date());


        Date currentDate = null;
        try {
            currentDate = dateFormat.parse(formattedDate);
            System.out.println(currentDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Message newMessage = new Message(selectedName, currentDate, currentDate, messageContent, myMacAddress, selectedMacAddress);

        // Créer un FileInputStream pour lire le fichier
        String filePath = getApplicationContext().getFilesDir().getPath() + "/chat.json";
        FileInputStream fileInputStream = new FileInputStream(filePath);

        // Lire le contenu du fichier en tant que chaîne de caractères
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line);
        }
        String jsonString = stringBuilder.toString();

        // Convertir la chaîne JSON en un objet JSONArray
        JSONArray jsonArray = new JSONArray(jsonString);

        // Fermer le flux de lecture
        bufferedReader.close();
        fileInputStream.close();

        // Ajouter le nouvel objet message au tableau JSON existant
        JSONObject messageObject = new JSONObject();
        messageObject.put("macAddressSrc", newMessage.getMacAddressSrc());
        messageObject.put("macAddressDest", newMessage.getMacAddressDest());
        messageObject.put("sentDate", newMessage.getFormattedSentDate());
        messageObject.put("receivedDate", newMessage.getFormattedReceivedDate());
        messageObject.put("content", newMessage.getName());
        jsonArray.put(messageObject);
        System.out.println(jsonArray);

        // Écrire le JSONArray dans le fichier
        try {
            FileWriter fileWriter = new FileWriter(filePath);
            fileWriter.write(jsonArray.toString());
            messageEditText.setText("");
            fileWriter.flush();
            fileWriter.close();
            // Effacer le champ de texte après l'envoi du message
        } catch (IOException e) {
            e.printStackTrace();
        }


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