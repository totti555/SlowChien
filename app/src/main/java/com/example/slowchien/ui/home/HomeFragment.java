package com.example.slowchien.ui.home;

import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.slowchien.MainActivity;
import com.example.slowchien.R;
import com.example.slowchien.databinding.FragmentHomeBinding;
import com.example.slowchien.ui.location.JSONUtils;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private ViewPager tabHost;
    private final int[] tabIcons = {
            R.drawable.ic_email_24,
            R.drawable.ic_send_24,
            R.drawable.ic_question_24
    };
    private static final String MESSAGES_FILE = "messages.json";
    private static final String CONTACTS_FILE = "contacts.json";
    private static final String JSON_DIRECTORY = "json";
    private Dialog dialog;
    String macAddressDest;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        //HomeViewModel homeViewModel =
        //        new ViewModelProvider(this).get(HomeViewModel.class);
        //View root = binding.getRoot();

        TabLayout.Tab tab;

        View view = inflater.inflate(R.layout.fragment_home,container, false);
        // Setting ViewPager for each Tabs
        ViewPager viewPager =  view.findViewById(R.id.view_pager);
        setupViewPager(viewPager);
        // Set Tabs inside Toolbar
        TabLayout tabs =  view.findViewById(R.id.tabLayout);
        tabs.setupWithViewPager(viewPager);

        //set new message button text
        //final TextView textBtnNewMessage = binding.newMessage;
        //homeViewModel.getNewMessageBtnLib().observe(getViewLifecycleOwner(), textBtnNewMessage::setText);

        // Récupération de la référence au bouton de scan

        Button newMessageButton = (Button) view.findViewById(R.id.newMessageBtn);
        newMessageButton.setOnClickListener(v -> {

            //Fragment fragment = new NewMessageFragment();
            //fragment.setArguments(args);

            /*
            Intent intent = new Intent(getActivity(), NewMessageActivity.class);
            intent.putExtra("pageName", "Nouveau Message");

            startActivity(intent);
            */
            try {
                showAddContactDialog();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
/*
        RelativeLayout.LayoutParams newMessageButtonRelativeLayout =
                new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        //add rules
        newMessageButtonRelativeLayout.addRule(RelativeLayout.CENTER_HORIZONTAL);
        newMessageButtonRelativeLayout.topMargin = 1000;

        newMessageButton.setLayoutParams(newMessageButtonRelativeLayout);
*/
        // Set icons of the tab bar
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabs));
        tabs.setupWithViewPager(viewPager);

        for (int i=0; i<tabs.getTabCount();i++) {
            if(tabs.getTabAt(i) != null) {
                tabs.getTabAt(i).setIcon(tabIcons[i]);
            }
        }

        return view;
    }

    private void showAddContactDialog() throws JSONException {
        dialog = new Dialog(requireContext(), R.style.RoundDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(getLayoutInflater().inflate(R.layout.message_dialog,null));

        int width = getResources().getDisplayMetrics().widthPixels;
        int dialogWidth = (int) (width * 0.9); // 70% de la largeur de l'écran
        int dialogHeight = WindowManager.LayoutParams.WRAP_CONTENT;

        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.width = dialogWidth;
            params.height = dialogHeight;
            window.setAttributes(params);
            window.setGravity(Gravity.CENTER); // Aligner le dialogue au centre
        }

        // Ajouter du padding à gauche et à droite dans le contenu du dialogue
        View dialogContent = dialog.findViewById(R.id.dialog_content);
        int paddingStartEnd = (int) (width * 0.05); // 10% de la largeur de l'écran
        dialogContent.setPadding(paddingStartEnd, paddingStartEnd, paddingStartEnd, paddingStartEnd);


        EditText editTextContent = dialog.findViewById(R.id.editTextContent);
        Button buttonAddContact = dialog.findViewById(R.id.buttonAddContact);
        Spinner spinnerContacts = dialog.findViewById(R.id.spinnerContacts);

        // Populate the spinner with contact names from the JSON file
        List<String> contactNames = getContactNamesFromJSON();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, contactNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerContacts.setAdapter(adapter);

        spinnerContacts.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Obtenez le nom de contact sélectionné
                String selectedContactName = adapter.getItem(position);

                // Recherchez le macAddress correspondant dans le JSON
                String macAddress = getMacAddressForContactName(selectedContactName);

                // Affichez le macAddress dans le TextView
                TextView textViewMacAddress = dialog.findViewById(R.id.textViewMacAddress);
                textViewMacAddress.setText(macAddress);

                // Stockez la valeur de macAddress dans macAddressDest
                macAddressDest = macAddress;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Gérer l'événement de rien sélectionné si nécessaire
            }
        });


        buttonAddContact.setOnClickListener(buttonView -> {
            String content = editTextContent.getText().toString();

            try {
                sendMessage(content,MESSAGES_FILE);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            dialog.dismiss();
        });

        dialog.show();
    }

    private List<String> getContactNamesFromJSON() {
        List<String> contactNames = new ArrayList<>();

        try {
            File directory = new File(requireContext().getFilesDir(), JSON_DIRECTORY);
            File file = new File(directory, CONTACTS_FILE);

            String jsonString = JSONUtils.loadJSONFromFile(file.getAbsolutePath());
            JSONArray jsonArray = new JSONArray(jsonString);

            // Extract contact names from each JSON object
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String name = jsonObject.getString("name");

                // Exclude contacts with name "User"
                if (!name.equals("User")) {
                    contactNames.add(name);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return contactNames;
    }

    private String getMacAddressForContactName(String contactName) {
        try {
            File directory = new File(requireContext().getFilesDir(), JSON_DIRECTORY);
            File file = new File(directory, CONTACTS_FILE);

            String jsonString = JSONUtils.loadJSONFromFile(file.getAbsolutePath());
            JSONArray jsonArray = new JSONArray(jsonString);

            // Search for the contact with the given name and return its macAddress
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String name = jsonObject.getString("name");
                String macAddress = jsonObject.getString("macAddress");

                if (name.equals(contactName)) {
                    return macAddress;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return ""; // Return an empty string if the macAddress is not found
    }


    private void sendMessage(String messageContent, String filePath) throws IOException, JSONException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        String formattedDate = dateFormat.format(new Date());
        String jsonDirectoryPath = getContext().getFilesDir().getAbsolutePath() + "/json";
        String messageFilePath = jsonDirectoryPath + "/" + MESSAGES_FILE;

        Date currentDate = null;
        try {
            currentDate = dateFormat.parse(formattedDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Spinner spinnerContacts = dialog.findViewById(R.id.spinnerContacts);
        String selectedContact = spinnerContacts.getSelectedItem().toString();
        Message newMessage = new Message(selectedContact, currentDate, currentDate, messageContent,MainActivity.getMacAddr(getContext()), macAddressDest);

        File directory = new File(requireContext().getFilesDir(), JSON_DIRECTORY);
        File file = new File(directory, MESSAGES_FILE);
        String jsonString = JSONUtils.loadJSONFromFile(file.getAbsolutePath());
        JSONArray jsonArray = new JSONArray(jsonString);
        JSONObject messageObject = new JSONObject();
        messageObject.put("name", newMessage.getTitle());
        messageObject.put("macAddressSrc", newMessage.getMacAddressSrc());
        messageObject.put("macAddressDest", newMessage.getMacAddressDest());
        messageObject.put("sentDate", newMessage.getFormattedSentDate());
        messageObject.put("receivedDate", newMessage.getFormattedReceivedDate());
        messageObject.put("content", newMessage.getName());

        jsonArray.put(messageObject);

        FileWriter fileWriter = new FileWriter(messageFilePath);
        fileWriter.write(jsonArray.toString());
        fileWriter.flush();
        fileWriter.close();

        JSONUtils.updateChatJSON(getContext());


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

    private void setupViewPager(ViewPager viewPager) {
        Adapter adapter = new Adapter(getChildFragmentManager());

        SentFragment sentFragment = new SentFragment();
        Bundle sentArgs = new Bundle();
        sentArgs.putString("id", "Envoyés");
        sentFragment.setArguments(sentArgs);
        adapter.addFragment(new ReceiveFragment(), "Reçus");
        adapter.addFragment(new SentFragment(), "Envoyés");
        adapter.addFragment(new ChatFragment(), "Chat");


        viewPager.setAdapter(adapter);
    }

    static class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public Adapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }


}