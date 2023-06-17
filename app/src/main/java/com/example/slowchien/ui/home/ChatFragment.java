package com.example.slowchien.ui.home;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.slowchien.MainActivity;
import com.example.slowchien.R;
import com.example.slowchien.ui.location.JSONUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class ChatFragment extends Fragment {

    private ListView mListView;
    private List<JSONObject> filteredList;
    private static final String JSON_DIRECTORY = "json";
    private static final String CHAT_FILE = "chat.json";

    private Handler mHandler;
    private static final long REFRESH_INTERVAL = 5000; // 5 secondes
    private int lastVisibleItemPosition = 0;

    private MessageAdapter adapter;

    public ChatFragment() {
        // Required empty public constructor
    }

    private static List<JSONObject> filterMessages(Context context, JSONArray jsonArray) throws JSONException {
        Map<String, JSONObject> latestMessages = new HashMap<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            // Définit l'adresse mac de l'utilisateur
            String macAddressSrc = MainActivity.getMacAddr(context).equals(jsonObject.getString("macAddressSrc"))
                    ? jsonObject.getString("macAddressDest")
                    : jsonObject.getString("macAddressSrc");

            String sentDate = jsonObject.getString("sentDate");

            if (!latestMessages.containsKey(macAddressSrc) || isSentDateNewer(sentDate, latestMessages.get(macAddressSrc).getString("sentDate"))) {
                latestMessages.put(macAddressSrc, jsonObject);
            }
        }

        return new ArrayList<>(latestMessages.values());
    }

    public static String getLatestMessageContent(String messagesJson, String macAddressSrc, String stringParam) {
        try {
            JSONArray messagesArray = new JSONArray(messagesJson);
            List<JSONObject> filteredMessages = new ArrayList<>();

            for (int i = 0; i < messagesArray.length(); i++) {
                JSONObject message = messagesArray.getJSONObject(i);
                String srcAddress = message.getString("macAddressSrc");
                String destAddress = message.getString("macAddressDest");

                if (srcAddress.equals(macAddressSrc) || destAddress.equals(macAddressSrc)) {
                    filteredMessages.add(message);
                }
            }

            if (filteredMessages.size() > 0) {
                SimpleDateFormat dateFormat;
                String dateStr = filteredMessages.get(0).getString("sentDate");

                if (dateStr.contains("GMT") ) {
                    dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss 'GMT' yyyy", Locale.ENGLISH);
                } else if (dateStr.contains(":")) {
                    dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                } else {
                    dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                }


                Date latestDate = dateFormat.parse(dateStr);

                JSONObject latestMessage = filteredMessages.get(0);

                for (int i = 1; i < filteredMessages.size(); i++) {
                    String currentDatesStr = filteredMessages.get(i).getString("sentDate");
                    if (currentDatesStr.contains("GMT") ) {
                        dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss 'GMT' yyyy", Locale.ENGLISH);
                    } else if (currentDatesStr.contains(":")) {
                        dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                    } else {
                        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    }
                    Date currentDate = dateFormat.parse(currentDatesStr);

                    if (currentDate.after(latestDate)) {
                        latestDate = currentDate;
                        latestMessage = filteredMessages.get(i);
                    }
                }
                if (Objects.equals(stringParam, "content")) {
                    return latestMessage.getString("content");
                }
                else if (Objects.equals(stringParam, "sentDate")) {
                    return latestMessage.getString("sentDate");
                }
                else {
                    return latestMessage.getString("receivedDate");
                }
            }
        } catch (JSONException | ParseException e) {
            e.printStackTrace();
        }

        return null;
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

    private List<Message> loadMessagesFromJson() {
        List<Message> messageList = new ArrayList<>();

        try {
            File directory = new File(requireContext().getFilesDir(), JSON_DIRECTORY);
            File file = new File(directory, CHAT_FILE);
            JSONUtils.créerChatJson(requireContext());
            String jsonString = JSONUtils.loadJSONFromFile(file.getAbsolutePath());

            JSONArray jsonArray = new JSONArray(jsonString);
            filteredList = filterMessages(getContext(),jsonArray);
            for (JSONObject jsonObject : filteredList) {
                String name = jsonObject.getString("name");
                String macAddressSrc = jsonObject.getString("macAddressSrc");
                String content = getLatestMessageContent(jsonString,macAddressSrc, "content");
                String receivedDateStr = getLatestMessageContent(jsonString,macAddressSrc, "receivedDate");
                String sentDateStr = getLatestMessageContent(jsonString,macAddressSrc, "sentDate");
                String macAddressDest = jsonObject.getString("macAddressDest");


                SimpleDateFormat inputFormat;
                assert receivedDateStr != null;
                if (receivedDateStr.contains("GMT") || sentDateStr.contains("GMT")) {
                    inputFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss 'GMT' yyyy", Locale.ENGLISH);
                } else if (sentDateStr.contains(":") || receivedDateStr.contains(":")) {
                    inputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                } else {
                    inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                }
                Date receivedDate = inputFormat.parse(receivedDateStr);
                assert sentDateStr != null;
                Date sentDate = inputFormat.parse(sentDateStr);
                messageList.add(new Message(content, receivedDate, sentDate, name, macAddressSrc, macAddressDest));
            }
            adapter = new MessageAdapter(getActivity(), messageList, "Chat");
        } catch (JSONException | ParseException e) {
            e.printStackTrace();
        }

        return messageList;
    }

    private void startRefreshing() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshData();
                // delai 5 secondes
                mHandler.postDelayed(this, REFRESH_INTERVAL);
            }
        }, REFRESH_INTERVAL);
    }

    private void refreshData() {
        // Scroll position and reload
        lastVisibleItemPosition = mListView.getFirstVisiblePosition();
        loadMessagesFromJson();
        mListView.setAdapter(adapter);
        mListView.setSelection(lastVisibleItemPosition);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        mListView = view.findViewById(R.id.simpleListView);
        loadMessagesFromJson();
        mListView.setAdapter(adapter);

        mListView.setOnItemClickListener((parent, v, position, id) -> {
            try {
                JSONObject selectedMessage = filteredList.get(position);

                String selectedMacAddress = MainActivity.getMacAddr(getContext()).equals(selectedMessage.getString("macAddressSrc"))
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

        mHandler = new Handler();
        startRefreshing();

        return view;
    }
}