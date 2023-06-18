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

import java.net.NetworkInterface;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private static final String MESSAGE_FILE = "messages.json";
    private static final String SENT_FILE = "sent.json";
    private static final String RECEIVED_FILE = "received.json";
    public static String MAC_ADDRESS="AA:AA:AA:AA:AA:AA";

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

        JSONUtils.initMessagesFile(getApplicationContext(),getMacAddr());
        JSONUtils.createSentReceiveJSON(getApplicationContext(), MESSAGE_FILE, SENT_FILE, "macAddressSrc");
        JSONUtils.createSentReceiveJSON(getApplicationContext(), MESSAGE_FILE, RECEIVED_FILE, "macAddressDest");
        JSONUtils.createChatJSON(getApplicationContext());
        JSONUtils.initMarkersFile(getApplicationContext());
        JSONUtils.initContactFile(getApplicationContext(),getMacAddr());
    }

    public static String getMacAddr() {
        if (!MAC_ADDRESS.equals("AA:AA:AA:AA:AA:AA")){
            return MAC_ADDRESS;
        }
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

    public static String getCurrentDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Date now = new Date();
        return dateFormat.format(now);
    }
}