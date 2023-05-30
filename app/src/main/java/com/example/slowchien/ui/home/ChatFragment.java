package com.example.slowchien.ui.home;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.slowchien.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

public class ChatFragment extends Fragment {

    private ListView mListView;
    private List<JSONObject> filteredList;
    static String myMacAddress = "FF-FF-FF-FF-FF-FF";

    public ChatFragment() {
        // Required empty public constructor
    }

    private static List<JSONObject> filterMessages(JSONArray jsonArray) throws JSONException {
        Map<String, JSONObject> latestMessages = new HashMap<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            // Définit l'adresse mac de l'utilisateur
            String macAddressSrc = myMacAddress.equals(jsonObject.getString("macAddressSrc"))
                    ? jsonObject.getString("macAddressDest")
                    : jsonObject.getString("macAddressSrc");

            String sentDate = jsonObject.getString("sentDate");

            if (!latestMessages.containsKey(macAddressSrc) || isSentDateNewer(sentDate, latestMessages.get(macAddressSrc).getString("sentDate"))) {
                latestMessages.put(macAddressSrc, jsonObject);
            }
        }

        return new ArrayList<>(latestMessages.values());
    }

    private static boolean isSentDateNewer(String date1, String date2) {
        if (date1 == null) {
            return false;
        } else if (date2 == null) {
            return true;
        } else {
            return date1.compareTo(date2) > 0;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        mListView = view.findViewById(R.id.simpleListView);
        // Inflate the layout for this fragment
        // Charger les messages depuis le fichier JSON
        List<Message> messageList = new ArrayList<>();

        try {
            InputStream inputStream = requireActivity().getAssets().open("chat.json");
            String jsonString = new Scanner(inputStream).useDelimiter("\\A").next();
            JSONArray jsonArray = new JSONArray(jsonString);
            filteredList = filterMessages(jsonArray);
            for (JSONObject jsonObject : filteredList) {
                String receivedDateStr = jsonObject.getString("receivedDate");
                String sentDateStr = jsonObject.getString("sentDate");
                String content = jsonObject.getString("content");
                String name = jsonObject.getString("name");
                String macAddress = myMacAddress.equals(jsonObject.getString("macAddressSrc"))
                        ? jsonObject.getString("macAddressDest")
                        : jsonObject.getString("macAddressSrc");

                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date receivedDate = inputFormat.parse(receivedDateStr);
                Date sentDate = inputFormat.parse(sentDateStr);
                messageList.add(new Message(content, receivedDate, sentDate, name, macAddress));
            }
        } catch (IOException | JSONException | ParseException e) {
            e.printStackTrace();
        }


        MessageAdapter adapter = new MessageAdapter(getActivity(), messageList);
        mListView.setAdapter(adapter);


        mListView.setOnItemClickListener((parent, v, position, id) -> {
            try {
                JSONObject selectedMessage = filteredList.get(position);

                String selectedMacAddress = myMacAddress.equals(selectedMessage.getString("macAddressSrc"))
                        ? selectedMessage.getString("macAddressDest")
                        : selectedMessage.getString("macAddressSrc");
                String name = selectedMessage.getString("name");

                Intent intent = new Intent(getActivity(), ChatActivity.class);

                intent.putExtra("selectedMacAddress", selectedMacAddress);
                intent.putExtra("name", name);

                startActivity(intent);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        });

        return view;
    }
}