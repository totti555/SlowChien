package com.example.slowchien.ui.exchange;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.slowchien.R;
import com.example.slowchien.databinding.FragmentExchangeBinding;

import java.util.ArrayList;
import java.util.UUID;


public class ExchangeFragment extends Fragment {

    private FragmentExchangeBinding binding;
    private final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BroadcastReceiver receiver;

    private static final int REQUEST_ENABLE_BT = 1; //ou 456
    private static final int PERMISSION_REQUEST_FINE_LOCATION = 100;

    private ArrayList<String> mDeviceNames;
    private ArrayList<BluetoothDevice> mDevices;
    private ArrayAdapter<String> mAdapter;
    private AlertDialog mAlertDialog;

    // Définition des différents états de connexion
    private static final long STATE_DISCONNECTED = 0;
    private static final long STATE_CONNECTING = 1;
    private static final long STATE_CONNECTED = 2;

    // Ensemble des UUID spécifiques aux téléphones mobiles
    private static final String PROFILE_HFP     = "0000111E-0000-1000-8000-00805F9B34FB";
    private static final String PROFILE_A2DP    = "0000110A-0000-1000-8000-00805F9B34FB";
    private static final String PROFILE_PAN     = "00001115-0000-1000-8000-00805F9B34FB";
    private static final String PROFILE_HSP     = "00001108-0000-1000-8000-00805F9B34FB";
    private static final String PROFILE_HDP     = "00001432-0000-1000-8000-00805F9B34FB";

    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = (int) STATE_DISCONNECTED;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        ExchangeViewModel exchangeViewModel =
                new ViewModelProvider(this).get(ExchangeViewModel.class);

        binding = FragmentExchangeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialisation du texte de la page
        final TextView textView = binding.textExchange;
        exchangeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        // Initialisation du bouton de scan des périphériques
        final TextView textBtnBT = binding.findBluetoothPeriph;
        exchangeViewModel.getBluetoothBtnLib().observe(getViewLifecycleOwner(), textBtnBT::setText);

        // Initialisation du bouton de détection du téléphone
        final TextView textBtnVisi = binding.setDeviceVisibility;
        exchangeViewModel.getVisibilityBtnLib().observe(getViewLifecycleOwner(), textBtnVisi::setText);

        // Récupération de la référence au bouton de scan
        Button mScanButton = root.findViewById(R.id.findBluetoothPeriph);

        // Récupération de la référence au bouton de détection
        Button mVisibilityButton = root.findViewById(R.id.setDeviceVisibility);
        mVisibilityButton.setEnabled(true);

        // Vérification des permissions Bluettoth
        checkBTPermissions();

        // Initialisation du AlertDialog
        setupAlertDialog();

        // Ajout d'un écouteur sur le bouton de scan
        mScanButton.setOnClickListener(v -> startBluetoothDiscovery());

        // Ajout d'un écouteur sur le bouton de scan
        mVisibilityButton.setOnClickListener(v -> setupDeciveVisibility(mVisibilityButton));

        return root;
    }

    public void checkBTPermissions(){

        if (bluetoothAdapter == null) {
            // Le dispositif ne prend pas en charge Bluetooth
            Toast.makeText(getContext(), "Périphérique ne supportant pas le Bluetooth", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            // Le Bluetooth n'est pas activé, demander à l'utilisateur de l'activer
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return;
        }

        // Demander les permissions nécessaires pour accéder aux périphériques Bluetooth
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_FINE_LOCATION);
            return;
        }
    }

    @SuppressLint("MissingPermission")
    private void setupAlertDialog(){

        mDeviceNames = new ArrayList<>();
        mDevices = new ArrayList<>();
        mAdapter = new ArrayAdapter<>(this.getContext(), android.R.layout.simple_list_item_1, mDeviceNames);

        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
        builder.setTitle("Appareils disponibles");
        builder.setAdapter(mAdapter, null);
        builder.setPositiveButton("Fermer", null);
        builder.setAdapter(mAdapter, (dialog, which) -> {
            // Connexion au périphérique sélectionné
            BluetoothDevice device = mDevices.get(which);
            if(device.getName() != null){
                connectToDevice(device, this.getContext());
            } else {
                Toast.makeText(this.getContext(), "Connexion impossible\nPériphérique inconnu", Toast.LENGTH_SHORT).show();
            }
        });
        mAlertDialog = builder.create();
    }

    @SuppressLint("MissingPermission")
    private void startBluetoothDiscovery() {
        if (bluetoothAdapter != null) {
            // Vérifier si la recherche est déjà en cours
            if (bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.cancelDiscovery();
            }
            // Démarrer la recherche de périphériques Bluetooth
            Toast.makeText(getActivity(), "Scan en cours...", Toast.LENGTH_SHORT).show();
            mAlertDialog.show();
            bluetoothAdapter.startDiscovery();
        }
    }

    private void setupDeciveVisibility( Button btn){
        int requestCode = 1;
        int tpsVisiSecondes = 60;
        int tpsVisiMinutes = tpsVisiSecondes/60;
        int tpsVisiMillisecondes = tpsVisiSecondes * 1000;

        btn.setEnabled(false);

        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, tpsVisiSecondes);

        startActivityForResult(discoverableIntent, requestCode);
        Toast.makeText(getActivity(), "Appareil visible pendant " + tpsVisiMinutes + "min (" + tpsVisiSecondes + "sec)", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(() -> btn.setEnabled(true), tpsVisiMillisecondes);
    }

    @SuppressLint("MissingPermission")
    public void connectToDevice(BluetoothDevice device, Context context) {
        // Vérification si le périphérique est déjà connecté
        if (mBluetoothGatt != null && mBluetoothGatt.getDevice().equals(device) && mConnectionState == STATE_CONNECTED) {
            Toast.makeText(context, "Périphérique déjà connecté !", Toast.LENGTH_SHORT).show();
            return;
        }

        // Fermeture de la connexion précédente s'il y en a une
        if (mBluetoothGatt != null) {
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }

        // Connexion au périphérique
        mBluetoothGatt = device.connectGatt(context, false, null);

        // Mise à jour de l'état de la connexion
        mConnectionState = (int) STATE_CONNECTING;

        // Enregistrement du temps de début de la tentative de connexion
        long mConnectionStartTime = System.currentTimeMillis();
    }

    @SuppressLint("MissingPermission")
    private void stopBluetoothDiscovery() {
        if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Register for broadcasts when a device is discovered.
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        receiver = new BroadcastReceiver() {
            @SuppressLint("MissingPermission")
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    // Périphérique détecté ! Récupération des infos depuis Intent
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    // Récupérer les informations contenues dans les services/UUID du périphérique
                    ParcelUuid[] tabUUIDs = device.getUuids();

                    // Booléen mis à jour lorsqu'un UUID spécifique à un téléphone mobile est détecté
                    boolean deviceIsMobilePhone = false;

                    if (tabUUIDs != null) {
                        for (ParcelUuid uuid : tabUUIDs) {
                            UUID u = uuid.getUuid();
                            switch (u.toString()){
                                case PROFILE_HFP:
                                case PROFILE_A2DP:
                                case PROFILE_PAN:
                                case PROFILE_HSP:
                                case PROFILE_HDP:
                                    deviceIsMobilePhone = true;
                                    break;
                                default:
                                    break;
                            }
                        }
                    }

                    String deviceName = device.getName(); // Nom du téléphone
                    String deviceHardwareAddress = device.getAddress(); // MAC address

                    if (deviceName != null) {
                        deviceName = "📱 " + deviceName;
                    }

                    String deviceInfo = deviceName + "\n📌 " + deviceHardwareAddress; //+ "\nRSSI: " + rssi;

                    // Si nouveau périphérique identifié comme un téléphone mobile est trouvé, on l'ajoute à la liste

////////////////// Si vous ne voulez QUE les téléphones mobiles, utilisez la ligne suivante et commentez la ligne 152
                    //if (deviceName != null && !mDeviceNames.contains(deviceInfo) && deviceIsMobilePhone) { // ONLY MOBILES

////////////////// Si vous voulez TOUS les périphériques détectés, sans filtrer leur type, utilisez la ligne suivante et commentez la ligne 148
                    if (deviceName != null && !mDeviceNames.contains(deviceInfo)) { // ALL DEVICES

                        mDeviceNames.add(deviceInfo);
                        mDevices.add(device);
                        mAdapter.notifyDataSetChanged();
                    }
                }
            }
        };
        getActivity().registerReceiver(receiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();

        // Arrêter la recherche de périphériques Bluetooth
        stopBluetoothDiscovery();

        // Unregister the ACTION_FOUND receiver.
        if (receiver != null) {
            getActivity().unregisterReceiver(receiver);
            receiver = null;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}