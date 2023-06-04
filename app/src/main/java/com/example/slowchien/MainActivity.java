package com.example.slowchien;

import android.os.Bundle;

import com.example.slowchien.ui.location.JSONUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.slowchien.databinding.ActivityMainBinding;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private static final String MESSAGE_FILE = "messages.json";
    private static final String SENT_FILE = "sent.json";
    private static final String RECEIVED_FILE = "received.json";
    private static final String CHAT_FILE = "chat.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_exchange, R.id.navigation_location, R.id.navigation_contact,R.id.navigation_settings)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        // Fonction pour clean tout le stockage interne (à décommenter si nécéssaire)
        // JSONUtils.cleanAllJSONFiles(getApplicationContext());

        initJSONFile(MESSAGE_FILE);
        JSONUtils.créerChatJson(getApplicationContext());
        JSONUtils.createSentReceiveJson(getApplicationContext(), MESSAGE_FILE, SENT_FILE, "macAddressSrc");
        JSONUtils.createSentReceiveJson(getApplicationContext(), MESSAGE_FILE, RECEIVED_FILE, "macAddressDest");


    }

    public void initJSONFile(String file){

        try {
            // Récupération du fichier JSON contenu dans le répertoire assets
            InputStream inputStream = getAssets().open(file);
            String jsonString = new Scanner(inputStream).useDelimiter("\\A").next();
            System.out.println(jsonString);
            // Copie du fichier JSON dans le stockage interne depuis le fichier assets
            JSONUtils.saveJsonFileToInternalStorage(getApplicationContext(), file, jsonString);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}