package com.example.slowchien.ui.home;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.slowchien.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SentFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SentFragment extends Fragment {


    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private ListView mListView;
    private String[] countryList;


     String mParam1;
     String mParam2;

    public SentFragment() {
        // Required empty public constructor
    }


    public static SentFragment newInstance(String param1, String param2) {
        SentFragment fragment = new SentFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sent, container, false);
        mListView = view.findViewById(R.id.simpleListView);
        countryList = new String[]{"Message 1", "Message 2", "Message 3", "Message 4", "Message 5", "Message 6"};

        // Créez un ArrayAdapter pour lier les données au ListView
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, countryList);

        // Liez l'adaptateur à la ListView
        mListView.setAdapter(arrayAdapter);

        return view;
    }
}