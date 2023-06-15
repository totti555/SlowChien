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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.NetworkInterface;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private static final String MESSAGE_FILE = "messages.json";
    private static final String SENT_FILE = "sent.json";
    private static final String RECEIVED_FILE = "received.json";
    private static final String JSON_DIRECTORY = "json";
    private static final String CONTACT_FILE = "contacts.json";

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
         JSONUtils.cleanAllJSONFiles(getApplicationContext());
        initMessageJSONFile(MESSAGE_FILE);
        initContactJSONFile(CONTACT_FILE);
        JSONUtils.créerChatJson(getApplicationContext());
        JSONUtils.createSentReceiveJson(getApplicationContext(), MESSAGE_FILE, SENT_FILE, "macAddressSrc");
        JSONUtils.createSentReceiveJson(getApplicationContext(), MESSAGE_FILE, RECEIVED_FILE, "macAddressDest");

        String toto = getMacAddr();
        System.out.println(toto);
    }

    public static String getMacAddr() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "AA:AA:AA:AA:AA:AA";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:",b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ignored) {
        }
        return "AA:AA:AA:AA:AA:AA";
    }

    public void initContactJSONFile(String file){

        try {
            // Récupération du fichier JSON contenu dans le répertoire assets
            InputStream inputStream = getAssets().open(file);
            String jsonString = new Scanner(inputStream).useDelimiter("\\A").next();
            // Copie du fichier JSON dans le stockage interne depuis le fichier assets
            JSONUtils.saveJsonFileToInternalStorage(getApplicationContext(), file, jsonString);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public void initMessageJSONFile(String file) {
        try {
            // Création de l'objet JSON
            File directory = new File(getApplicationContext().getFilesDir(), JSON_DIRECTORY);
            String filePath = directory + "/" + file;

                JSONArray jsonArray = new JSONArray();

                // Création du premier objet
                JSONObject userObject = new JSONObject();
                userObject.put("name", "SlowChien");
                userObject.put("receivedDate", getCurrentDateTime());
                userObject.put("sentDate", getCurrentDateTime());
                userObject.put("content", "Super ! Je suis sur Slowchien !");
                userObject.put("macAddressSrc", getMacAddr());
                userObject.put("macAddressDest", "AB:CD:EF:AB:CD:EF");


                // Création du deuxième objet
                JSONObject slowChienObject = new JSONObject();
                slowChienObject.put("name", "SlowChien");
                slowChienObject.put("receivedDate", getCurrentDateTime());
                slowChienObject.put("sentDate", getCurrentDateTime());
                slowChienObject.put("content", "Bienvenue dans Slowchien !");
                slowChienObject.put("macAddressSrc", "AB:CD:EF:AB:CD:EF");
                slowChienObject.put("macAddressDest", getMacAddr());


                jsonArray.put(slowChienObject);
                jsonArray.put(userObject);

                // Écriture du fichier JSON dans le stockage interne
                String jsonString = jsonArray.toString();
                JSONUtils.saveJsonFileToInternalStorage(getApplicationContext(), file, jsonString);


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String getCurrentDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Date now = new Date();
        return dateFormat.format(now);
    }
}