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
import java.util.List;
import java.util.Locale;
import java.util.Scanner;


public class ReceiveFragment extends Fragment {

    private ListView mListView;
    public ReceiveFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_receive, container, false);
        View viewHome = inflater.inflate(R.layout.fragment_home, container, false);
        mListView = view.findViewById(R.id.simpleListView);

        // Charger les messages depuis le fichier JSON
        List<Message> messageList = new ArrayList<>();
        try {
            InputStream inputStream = requireActivity().getAssets().open("receive.json");
            String jsonString = new Scanner(inputStream).useDelimiter("\\A").next();
            JSONArray jsonArray = new JSONArray(jsonString);
            System.out.println("JSON :" + jsonArray);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                String receivedDateStr = jsonObject.getString("receivedDate");
                String sentDateStr = jsonObject.getString("sentDate");
                String content = jsonObject.getString("content");
                String name = "De: " + jsonObject.getString("name");
                String macAddress = jsonObject.getString("macAddressSrc");

                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date receivedDate = inputFormat.parse(receivedDateStr);
                Date sentDate = inputFormat.parse(sentDateStr);

                System.out.println(new Message(content, receivedDate, sentDate, name, macAddress));
                messageList.add(new Message(content, receivedDate, sentDate, name, macAddress));
            }
        } catch (IOException | JSONException | ParseException e) {
            e.printStackTrace();
        }

        // Créer l'adaptateur personnalisé avec la liste de messages
        MessageAdapter adapter = new MessageAdapter(getActivity(), messageList);

        // Attacher l'adaptateur à la ListView
        mListView.setAdapter(adapter);

        // Afficher le contenu d'un message lors d'un click
        mListView.setOnItemClickListener((parent, v, position, id) -> {
            Message message = messageList.get(position);

            Bundle args = new Bundle();
            args.putParcelable("message", (Parcelable) message);

            Fragment fragment = new MessageDetailsFragment();
            fragment.setArguments(args);

            Intent myIntent = new Intent(view.getContext(), MessageDetailsActivity.class);

            System.out.println(message);
            myIntent.putExtra("messageData", message);
            myIntent.putExtra("pageName", "Message reçu");
            view.getContext().startActivity(myIntent);

        });

        return view;
    }
}