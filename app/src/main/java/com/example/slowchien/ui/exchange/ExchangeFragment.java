package com.example.slowchien.ui.exchange;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.slowchien.R;
import com.example.slowchien.databinding.FragmentExchangeBinding;
import com.example.slowchien.ui.location.JSONUtils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.UUID;


public class ExchangeFragment extends Fragment {

    private FragmentExchangeBinding binding;
    private final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BroadcastReceiver receiver;

    public Button mScanButton;
    public Button mVisibilityButton;
    public Button mDisconnectButton;
    public Button mSendingButton;

    private static final int REQUEST_ENABLE_BT = 1;

    private static final int PERMISSION_SCAN_REQUEST = 123;
    private static final int PERMISSION_VISI_REQUEST = 321;

    private ArrayList<String> mDeviceNames;
    private ArrayList<BluetoothDevice> mDevices;
    private ArrayAdapter<String> mAdapter;
    private AlertDialog mAlertDialog;


    public BluetoothDevice selectedDevice = null;

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String MARKERS_FILE = "markers.json";


    BluetoothSocket socket;



    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        ExchangeViewModel exchangeViewModel =
                new ViewModelProvider(this).get(ExchangeViewModel.class);

        binding = FragmentExchangeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialisation du bouton de scan des périphériques
        final TextView textBtnBT = binding.findBluetoothPeriph;
        exchangeViewModel.getBluetoothBtnLib().observe(getViewLifecycleOwner(), textBtnBT::setText);

        // Initialisation du bouton de détection du téléphone
        final TextView textBtnVisi = binding.setDeviceVisibility;
        exchangeViewModel.getVisibilityBtnLib().observe(getViewLifecycleOwner(), textBtnVisi::setText);

        // Initialisation du bouton de déconnexion des téléphones
        final TextView textBtnDeco = binding.disconnectDevices;
        exchangeViewModel.getDisconnectBtnLib().observe(getViewLifecycleOwner(), textBtnDeco::setText);

        // Initialisation du bouton d'envoi des fichiers
        final TextView textBtnSend = binding.sendFile;
        exchangeViewModel.getSendingBtnLib().observe(getViewLifecycleOwner(), textBtnSend::setText);

        // Configuration du popup listant les périphériques trouvés
        setupAlertDialog();

        // Initialisation du bouton de recherche de périphériques
        mScanButton = root.findViewById(R.id.findBluetoothPeriph);

        // Initialisation du bouton de détection du téléphone
        mVisibilityButton = root.findViewById(R.id.setDeviceVisibility);
        mVisibilityButton.setEnabled(true);

        // Initialisation du bouton de déconnexion
        mDisconnectButton = root.findViewById(R.id.disconnectDevices);
        mDisconnectButton.setVisibility(View.GONE);

        // Initialisation du bouton d'envoi de fichiers
        mSendingButton = root.findViewById(R.id.sendFile);
        mSendingButton.setVisibility(View.GONE);

        // Configuration de l'action du bouton de scan
        mScanButton.setOnClickListener(v -> checkScanPermissions());

        // Configuration de l'action du bouton de visibilitié
        mVisibilityButton.setOnClickListener(v -> checkVisibilityPermissions());

        // Configuration de l'action du bouton de déconnexion
        mDisconnectButton.setOnClickListener(v -> closeBluetoothConnection());

        // Configuration de l'action du bouton d'envoi
        mSendingButton.setOnClickListener(v -> {
            try {
                sendJSONFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        return root;
    }

    public void checkBTConfigAndRun(String execFunction){

        if (bluetoothAdapter == null) {
            // Appareil ne prenant pas en charge le Bluetooth
            Toast.makeText(getContext(), "Périphérique ne supportant pas le Bluetooth", Toast.LENGTH_SHORT).show();
            return;
        }

        // Bluetooth désactivé
        if (!bluetoothAdapter.isEnabled()) {
            // Demander d'activation du Bluetooth
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            // Fonction à appeler après vérification du bluetooth
            switch (execFunction){
                case "SCAN":
                    // Lancement de la recherche de périphériques alentours
                    startBluetoothDiscovery();
                    break;
                case "VISI":
                    // Changement d'état de détection du téléphone
                    setupDeciveVisibility();
                    break;
                default:
                    break;
            }
        }

    }

    private void checkScanPermissions() {

        // Vérification des permissions BLUETOOTH_CONNECT et BLUETOOTH_SCAN
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

                    String[] permissions = {Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN};
                    requestPermissions(permissions, PERMISSION_SCAN_REQUEST);

                } else {
                    requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_SCAN_REQUEST);
                }
            } else {
                requestPermissions(new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_SCAN_REQUEST);
            }
        }
    }

    private void checkVisibilityPermissions() {

        // Vérification des permissions BLUETOOTH_CONNECT et ACCESS_FINE_LOCATION
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

                    String[] permissions = {Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.ACCESS_FINE_LOCATION};
                    requestPermissions(permissions, PERMISSION_VISI_REQUEST);

                } else {
                    requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_VISI_REQUEST);
                }
            } else {
                requestPermissions(new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_VISI_REQUEST);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_SCAN_REQUEST:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                                // Permission accordée, lancement du scan
                                checkBTConfigAndRun("SCAN");

                            } else {
                                // Permission refusée
                                Toast.makeText(requireContext(), "Permission Scan Bluetooth refusée", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            if (grantResults.length > 0 && grantResults[0]
                                    == PackageManager.PERMISSION_GRANTED) {

                                // Permission accordée, lancement du scan
                                checkBTConfigAndRun("SCAN");

                            } else {
                                // Permission refusée
                                Toast.makeText(requireContext(), "Permission Scan Bluetooth refusée", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
                break;
            case PERMISSION_VISI_REQUEST:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                                // Permission accordée, changement d'état
                                checkBTConfigAndRun("VISI");
                            }
                        } else {
                            if (grantResults.length > 0 && grantResults[0]
                                    == PackageManager.PERMISSION_GRANTED) {

                                // Permission accordée, changement d'état
                                checkBTConfigAndRun("VISI");
                            }
                        }
                    }
                }
                break;
            default:
                break;
        }
    }


    @SuppressLint("MissingPermission")
    private void startBluetoothDiscovery() {

        // Vérification de la config Bluetooth
        if (bluetoothAdapter != null) {
            // Vérification de l'état de la recherche
            if (bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.cancelDiscovery();
            }
            // Lancement de la recherche de périphériques Bluetooth
            Toast.makeText(getActivity(), "Scan en cours...", Toast.LENGTH_SHORT).show();
            mAlertDialog.show();
            bluetoothAdapter.startDiscovery();
        }
    }

    private void setupDeciveVisibility(){
        int requestCode = 1;
        int tpsVisiSecondes = 60;
        int tpsVisiMillisecondes = tpsVisiSecondes * 1000;

        // Désactivation du bouton de visibilité
        mVisibilityButton.setEnabled(false);

        // Appareil rendu détectable pendant 60 secondes
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, tpsVisiSecondes);

        startActivityForResult(discoverableIntent, requestCode);

        // Bouton restant désactivé pendant les 60 secondes
        new Handler().postDelayed(() -> mVisibilityButton.setEnabled(true), tpsVisiMillisecondes);

    }

    @SuppressLint("MissingPermission")
    private void setupAlertDialog(){

        mDeviceNames = new ArrayList<>();
        mDevices = new ArrayList<>();
        mAdapter = new ArrayAdapter<>(this.getContext(), android.R.layout.simple_list_item_1, mDeviceNames);

        // Configuration du popup listant les périphériques détecté
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
        builder.setTitle("Appareils disponibles");
        builder.setAdapter(mAdapter, null);
        builder.setPositiveButton("Fermer", null);
        builder.setAdapter(mAdapter, (dialog, which) -> {
            // Connexion au périphérique sélectionné
            BluetoothDevice device = mDevices.get(which);
            if(device.getName() != null){
                selectedDevice = device;
                // Si le périphérique n'est pas déjà appairé
                if (selectedDevice.getBondState() != BluetoothDevice.BOND_BONDED) {
                    selectedDevice.createBond();
                } else {
                    Toast.makeText(this.getContext(), "Périphérique déjà connecté !", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Si pas assez d'infos sur le périphérique ciblé
                Toast.makeText(this.getContext(), "Connexion impossible - Périphérique inconnu !", Toast.LENGTH_SHORT).show();
            }
        });
        mAlertDialog = builder.create();
    }

    @SuppressLint("MissingPermission")
    private void connectToSelectedDevice() {

        // Initialisation d'une connexion sécurisée avec le périphérique
        try {

            // Configuration du socket de connexion
            socket = selectedDevice.createRfcommSocketToServiceRecord(MY_UUID);

            Toast.makeText(this.getContext(), "Connexion établie !", Toast.LENGTH_SHORT).show();

            // Une fois la connexion établie
            // Disparition des boutons de scan et de visibilité du téléphone
            mScanButton.setVisibility(View.GONE);
            mVisibilityButton.setVisibility(View.GONE);

            // Affichage des boutons de déconnexion et d'envoi de fichier
            mDisconnectButton.setVisibility(View.VISIBLE);
            mSendingButton.setVisibility(View.VISIBLE);

            // Connexion au périphérique ciblé
            socket.connect();

        } catch (IOException e) {
            // Erreur lors de la connexion
            Toast.makeText(this.getContext(), "Erreur de connexion !", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("MissingPermission")
    private void closeBluetoothConnection(){
        try {
            if(selectedDevice != null){
                // Fermer la connexion BluetoothSocket
                socket.close();

                // Si le périphérique est appairé
                if (selectedDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                    // Dissociation du périphérique
                    try {
                        Method method = selectedDevice.getClass().getMethod("removeBond");
                        method.invoke(selectedDevice);

                        Toast.makeText(requireContext(), "Déconnexion réussie !", Toast.LENGTH_SHORT).show();

                        // Une fois la connexion rompue
                        // Affichage des boutons de scan et de visibilité du téléphone
                        mScanButton.setVisibility(View.VISIBLE);
                        mVisibilityButton.setVisibility(View.VISIBLE);

                        // Disparition des boutons de déconnexion et d'envoi de fichier
                        mDisconnectButton.setVisibility(View.GONE);
                        mSendingButton.setVisibility(View.GONE);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    // Si le périphérique n'est pas appairé
                    Toast.makeText(requireContext(), "Périphérique pas appairé !", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendJSONFile() throws IOException {

        Toast.makeText(requireContext(), "Envoi des données en cours...", Toast.LENGTH_SHORT).show();

        // Récupération du fichier JSON contenu dans le répertoire assets
        InputStream inputStream = requireActivity().getAssets().open(MARKERS_FILE);
        String jsonString = new Scanner(inputStream).useDelimiter("\\A").next();

        // Envoi du fichier (?)
        JSONUtils.saveJsonToFile(requireContext(), jsonString, MARKERS_FILE);
    }


    @SuppressLint("MissingPermission")
    private void stopBluetoothDiscovery() {

        // Si l'appareil est en recherche de périphériques
        if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering()) {

            // Arrêt de la recherche
            bluetoothAdapter.cancelDiscovery();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Initialisation des broadcasts utilisés lorsqu'un périphérique est détecté
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        IntentFilter bondFilter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        receiver = new BroadcastReceiver() {
            @SuppressLint("MissingPermission")
            @Override
            public void onReceive(Context context, Intent intent) {

                String action = intent.getAction();

                switch(action) {
                    case BluetoothDevice.ACTION_FOUND:
                        // Périphérique détecté ! Récupération des infos depuis Intent
                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        BluetoothClass deviceClass = device.getBluetoothClass();

                        // Booléen mis à jour lorsqu'un UUID spécifique à un téléphone mobile est détecté
                        boolean deviceIsMobilePhone = deviceClass != null && deviceClass.getMajorDeviceClass() == BluetoothClass.Device.Major.PHONE;

                        String deviceName = device.getName(); // Nom du périphérique
                        String deviceHardwareAddress = device.getAddress(); // Adresse MAC du périphérique

                        // Gestion de l'affichage dans le builder AlertDialog
                        if(deviceName == null){
                            deviceName = "❔ Inconnu";
                        } else if (deviceIsMobilePhone) {
                            deviceName = "📱 " + device.getName();
                        } else {
                            deviceName = "❔ " + device.getName();
                        }
                        String deviceInfo = deviceName + "\n📌 " + deviceHardwareAddress;

                        // Si nouveau périphérique identifié comme un téléphone mobile est trouvé, on l'ajoute à la liste
                        if (!mDeviceNames.contains(deviceInfo) && deviceIsMobilePhone) {

                            mDeviceNames.add(deviceInfo);
                            mDevices.add(device);
                            mAdapter.notifyDataSetChanged();
                        }
                        break;
                    case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
                        // Changement d'état d'appairage
                        BluetoothDevice bondedDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);

                        if (bondedDevice.equals(selectedDevice)) {
                            if (bondState == BluetoothDevice.BOND_BONDED) {
                                // Périphérique appairé, connexion au périphérique
                                connectToSelectedDevice();
                            }  // Echec de l'appairage

                        }
                        break;
                    default:
                        break;
                }
            }
        };
        requireActivity().registerReceiver(receiver, filter);
        requireActivity().registerReceiver(receiver, bondFilter);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (receiver != null) {
            requireActivity().unregisterReceiver(receiver);
            receiver = null;
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Arrêt de la recherche de périphériques Bluetooth
        if(ContextCompat.checkSelfPermission(this.requireContext(), Manifest.permission.BLUETOOTH_CONNECT)
                == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(this.requireContext(), Manifest.permission.BLUETOOTH_SCAN)
                        == PackageManager.PERMISSION_GRANTED){
            if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering()) {
                stopBluetoothDiscovery();
            }
        }

        // Si appareil connecté à un périphérique, rupture de la connexion
        if(selectedDevice != null){
            closeBluetoothConnection();
        }

        binding = null;
    }
}