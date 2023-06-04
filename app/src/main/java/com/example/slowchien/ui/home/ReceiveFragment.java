package com.example.slowchien.ui.home;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
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
import java.util.List;
import java.util.Locale;

public class ReceiveFragment extends Fragment {

    private ListView mListView;
    private static final String JSON_DIRECTORY = "json";
    private static final String RECEIVE_FILE = "received.json";

    private Handler mHandler;
    private static final long REFRESH_INTERVAL = 5000; // 5 secondes
    private int lastVisibleItemPosition = 0;

    private List<Message> messageList;
    private MessageAdapter adapter;

    public ReceiveFragment() {
        // Required empty public constructor
    }

    private void loadMessagesFromJson() {
        // Charger les messages depuis le fichier JSON
        messageList = new ArrayList<>();

        try {
            File directory = new File(requireContext().getFilesDir(), JSON_DIRECTORY);
            File file = new File(directory, RECEIVE_FILE);

            String jsonString = JSONUtils.loadJSONFromFile(file.getAbsolutePath());
            JSONArray jsonArray = new JSONArray(jsonString);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String receivedDateStr = jsonObject.getString("receivedDate");
                String sentDateStr = jsonObject.getString("sentDate");
                String content = jsonObject.getString("content");
                String name = "De: " + jsonObject.getString("name");
                String macAddressSrc = jsonObject.getString("macAddressSrc");
                String macAddressDest = jsonObject.getString("macAddressDest");
                SimpleDateFormat inputFormat;
                if (receivedDateStr.contains("GMT") || sentDateStr.contains("GMT")) {
                    inputFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss 'GMT' yyyy", Locale.ENGLISH);
                } else if (sentDateStr.contains(":") || receivedDateStr.contains(":")) {
                    inputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                } else {
                    inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                }
                Date receivedDate = inputFormat.parse(receivedDateStr);
                Date sentDate = inputFormat.parse(sentDateStr);
                messageList.add(new Message(content, receivedDate, sentDate, name, macAddressSrc, macAddressDest));
            }
            JSONUtils.sortMessagesByNewestDate(messageList, "Received");
            adapter = new MessageAdapter(getActivity(), messageList, "Received");
        } catch (JSONException | ParseException e) {
            e.printStackTrace();
        }
    }

    private void startRefreshing() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshData();
                // Planifier le prochain rafraîchissement après l'intervalle défini
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
        View view = inflater.inflate(R.layout.fragment_receive, container, false);
        mListView = view.findViewById(R.id.simpleListView);
        loadMessagesFromJson();
        mListView.setAdapter(adapter);

        mListView.setOnItemClickListener((parent, v, position, id) -> {
            Message message = messageList.get(position);

            Bundle args = new Bundle();
            args.putParcelable("message", message);

            Fragment fragment = new MessageDetailsFragment();
            fragment.setArguments(args);

            Intent myIntent = new Intent(view.getContext(), MessageDetailsActivity.class);
            myIntent.putExtra("messageData", message);
            myIntent.putExtra("pageName", "Message reçu");
            view.getContext().startActivity(myIntent);
        });

        mHandler = new Handler();
        startRefreshing();

        return view;
    }
}