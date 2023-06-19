package com.example.slowchien.ui.exchange;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.slowchien.R;
import com.example.slowchien.databinding.FragmentExchangeBinding;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;


public class ExchangeFragment extends Fragment {

    private FragmentExchangeBinding binding;
    private final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BroadcastReceiver receiver;

    public Button mScanButton;
    public Button mPairedDevicesButton;
    public Button mVisibilityButton;
    public Button mDisconnectButton;
    public Button mSendingButton;

    private static final int PERMISSION_SCAN_REQUEST = 123;
    private static final int PERMISSION_VISI_REQUEST = 321;
    private static final int PERMISSION_LINK_REQUEST = 666;

    private ArrayList<String> mDeviceNames;
    private ArrayList<BluetoothDevice> mDevices;
    private ArrayAdapter<String> mAdapter;
    private AlertDialog mScanAlertDialog;

    private ArrayList<BluetoothDevice> mPairedDevices;


    public BluetoothDevice selectedDevice = null;

    private static final String MARKERS_FILE = "markers.json";

    BluetoothService bluetoothService;

    private static final String NAME = "SlowChien";
    private static final String TAG = "BluetoothService";
    private static final UUID MY_UUID =
            UUID.fromString("a6d1e32d-f043-4a0b-95dd-a5c26ba916c3");

    private Handler mHandler;

    // #defines for identifying shared types between calling functions
    private final static int REQUEST_ENABLE_BT = 1; // used to identify adding bluetooth names
    public final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message update
    private final static int CONNECTING_STATUS = 3; // used in bluetooth handler to identify message status

    boolean isConnected = false;

    ServerThread serverThread = null;
    ClientThread clientThread = null;
    ConnectedThread connectedThread = null;


    @RequiresApi(api = Build.VERSION_CODES.O)
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        ExchangeViewModel exchangeViewModel =
                new ViewModelProvider(this).get(ExchangeViewModel.class);

        binding = FragmentExchangeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();



        // Initialisation du bouton de scan des périphériques
        final TextView textBtnBT = binding.findBluetoothPeriph;
        exchangeViewModel.getBluetoothBtnLib().observe(getViewLifecycleOwner(), textBtnBT::setText);

        // Initialisation du bouton de listing des périphériques appairés
        final TextView textBtnPairedDevices = binding.getPairedBluetoothPeriph;
        exchangeViewModel.getPairedDevicesBtnLib().observe(getViewLifecycleOwner(), textBtnPairedDevices::setText);

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
        setupAlertDialogScan();

        // Initialisation du bouton de recherche de périphériques
        mScanButton = root.findViewById(R.id.findBluetoothPeriph);

        // Initialisation du bouton de listing des périphériques appairés
        mPairedDevicesButton = root.findViewById(R.id.getPairedBluetoothPeriph);

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

        // Configuration de l'action du bouton de listing
        mPairedDevicesButton.setOnClickListener(v -> checkPairedDevicesPermissions());

        // Configuration de l'action du bouton de visibilitié
        mVisibilityButton.setOnClickListener(v -> checkVisibilityPermissions());

        // Configuration de l'action du bouton de déconnexion
        mDisconnectButton.setOnClickListener(v -> closeBluetoothConnection());

        // Configuration de l'action du bouton d'envoi
        mSendingButton.setOnClickListener(v -> {
            try {
                if(selectedDevice != null){
                    sendJSONData();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        mHandler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg){
                if(msg.what == MESSAGE_READ){
                    String readMessage;
                    readMessage = new String((byte[]) msg.obj, StandardCharsets.UTF_8);
                    System.out.println((">>> Message : " + readMessage));
                }

                if(msg.what == CONNECTING_STATUS){
                    if(msg.arg1 == 1)
                        System.out.println(">>> Bluetooth CONNECTED : " + msg.obj);
                    else
                        System.out.println(">>> Echec connexion");
                }
            }
        };

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

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void checkPairedDevicesPermissions() {

        // Vérification des permissions BLUETOOTH_CONNECT et BLUETOOTH_SCAN
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

                    if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.BLUETOOTH_CONNECT)
                            == PackageManager.PERMISSION_GRANTED) {

                        setupAlertDialogPaired();

                    } else {

                        String[] permissions = {Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN};
                        requestPermissions(permissions, PERMISSION_LINK_REQUEST);
                    }

                } else {
                    setupAlertDialogPaired();
                }
            } else {
                setupAlertDialogPaired();
            }
        } else {
            setupAlertDialogPaired();
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
        } else {
            requestPermissions(new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_SCAN_REQUEST);
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
        } else {
            requestPermissions(new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_VISI_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_LINK_REQUEST:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                                // Permission accordée, lancement du scan
                                setupAlertDialogPaired();

                            } else {
                                // Permission refusée
                                Toast.makeText(requireContext(), "Accès appareils mobiles appairés refusée", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
                break;
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
                break;
            case PERMISSION_VISI_REQUEST:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                                // Permission accordée, changement d'état
                                checkBTConfigAndRun("VISI");
                            } else {
                                // Permission refusée
                                Toast.makeText(requireContext(), "Permission Visibilité refusée", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            if (grantResults.length > 0 && grantResults[0]
                                    == PackageManager.PERMISSION_GRANTED) {

                                // Permission accordée, changement d'état
                                checkBTConfigAndRun("VISI");
                            } else {
                                // Permission refusée
                                Toast.makeText(requireContext(), "Permission Visibilité refusée", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                            // Permission accordée, changement d'état
                            checkBTConfigAndRun("VISI");
                        } else {
                            // Permission refusée
                            Toast.makeText(requireContext(), "Permission Visibilité refusée", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                        // Permission accordée, changement d'état
                        checkBTConfigAndRun("VISI");
                    } else {
                        // Permission refusée
                        Toast.makeText(requireContext(), "Permission Visibilité refusée", Toast.LENGTH_SHORT).show();
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
            mScanAlertDialog.show();
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("MissingPermission")
    private void setupAlertDialogScan(){

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
                    // on enregistre le périphérique souhaité
                    selectedDevice = device;
                    // Si le périphérique n'est pas déjà appairé
                    if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                        selectedDevice = device;

                        selectedDevice.createBond();

                    }
                    // Si le périphérique est déjà appairé, pas besoin de créer la liaison
                    else {
                        selectedDevice = device;

                        if(serverThread != null){
                            serverThread.cancel();
                        }
                        serverThread = new ServerThread();
                        serverThread.start();

                        if(clientThread != null){
                            clientThread.cancel();
                        }
                        clientThread = new ClientThread(selectedDevice);
                        clientThread.start();

                    }
                } else {
                    // Si pas assez d'infos sur le périphérique ciblé
                    Toast.makeText(this.getContext(), "Connexion impossible - Périphérique inconnu !", Toast.LENGTH_SHORT).show();
                }
            });
            mScanAlertDialog = builder.create();

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("MissingPermission")
    private void setupAlertDialogPaired(){

        ArrayList<String> mPairedDevicesNames = new ArrayList<>();
        mPairedDevices = new ArrayList<>();

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        // Parcourir les périphériques appairés
        for (BluetoothDevice device : pairedDevices) {
            BluetoothClass deviceClass = device.getBluetoothClass();

            // Vérifier si le périphérique est un téléphone mobile
            if (deviceClass != null && deviceClass.getMajorDeviceClass() == BluetoothClass.Device.Major.PHONE) {

                String deviceName = device.getName(); // Nom du périphérique
                String deviceHardwareAddress = device.getAddress(); // Adresse MAC du périphérique

                // Gestion de l'affichage dans le builder AlertDialog
                if (deviceName == null) {
                    deviceName = "❔ Inconnu";
                } else {
                    deviceName = "📲 " + device.getName();
                }
                String deviceInfo = deviceName + "\n📌 " + deviceHardwareAddress;

                mPairedDevicesNames.add(deviceInfo);
                mPairedDevices.add(device);
            }
        }

        ArrayAdapter<String> mPairedAdapter = new ArrayAdapter<>(this.getContext(), android.R.layout.simple_list_item_1, mPairedDevicesNames);

        // Configuration du popup listant les périphériques détecté
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
        builder.setTitle("Appareils appairés");
        builder.setAdapter(mPairedAdapter, null);
        builder.setPositiveButton("Fermer", null);
        builder.setAdapter(mPairedAdapter, (dialog, which) -> {
            // Connexion au périphérique sélectionné
            BluetoothDevice device = mPairedDevices.get(which);
            if (device.getName() != null) {

                selectedDevice = device;

                if(serverThread != null){
                    serverThread.cancel();
                }
                serverThread = new ServerThread();
                serverThread.start();

                if(clientThread != null){
                    clientThread.cancel();
                }
                clientThread = new ClientThread(selectedDevice);
                clientThread.start();

            } else {
                // Si pas assez d'infos sur le périphérique ciblé
                Toast.makeText(this.getContext(), "Connexion impossible - Périphérique inconnu !", Toast.LENGTH_SHORT).show();
            }
        });

        AlertDialog mPairedAlertDialog = builder.create();
        mPairedAlertDialog.show();
    }

    @SuppressLint("MissingPermission")
    private void closeBluetoothConnection(){
        if(selectedDevice != null){
            // Fermer la connexion BluetoothSocket
            //socket.close();
            //connectedThread.cancel();

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
                    mPairedDevicesButton.setVisibility(View.VISIBLE);
                    mVisibilityButton.setVisibility(View.VISIBLE);

                    // Disparition des boutons de déconnexion et d'envoi de fichier
                    mDisconnectButton.setVisibility(View.GONE);
                    mSendingButton.setVisibility(View.GONE);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                // Si le périphérique n'est pas appairé
                Toast.makeText(requireContext(), "Périphérique non appairé !", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void sendJSONData() throws  IOException {

        Toast.makeText(requireContext(), "Envoi des données en cours...", Toast.LENGTH_SHORT).show();

        // Récupération du fichier JSON contenu dans le répertoire assets
        /*
        InputStream inputStream = requireActivity().getAssets().open(MARKERS_FILE);
        String jsonString = new Scanner(inputStream).useDelimiter("\\A").next();

        bluetoothService.write(jsonString.getBytes(StandardCharsets.UTF_8));
         */

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
            @RequiresApi(api = Build.VERSION_CODES.O)
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

                                if(serverThread != null){
                                    serverThread.cancel();
                                }
                                serverThread = new ServerThread();
                                serverThread.start();

                                if(clientThread != null){
                                    clientThread.cancel();
                                }
                                clientThread = new ClientThread(selectedDevice);
                                clientThread.start();

                                // Une fois la connexion établie
                                // Disparition des boutons de scan et de visibilité du téléphone
                                mScanButton.setVisibility(View.GONE);
                                mPairedDevicesButton.setVisibility(View.GONE);
                                mVisibilityButton.setVisibility(View.GONE);

                                // Affichage des boutons de déconnexion et d'envoi de fichier
                                mDisconnectButton.setVisibility(View.VISIBLE);
                                mSendingButton.setVisibility(View.VISIBLE);

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



    private class ServerThread extends Thread {
        BluetoothServerSocket serverSocket;

        @SuppressLint("MissingPermission")
        public ServerThread() {
            BluetoothServerSocket tmp = null;
            try {
                // Création d'un serveur BluetoothServerSocket
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
                Log.d("Socket", "Création server socket : " + MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Erreur lors de la création du BluetoothServerSocket", e);
            }
            serverSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            while(!isConnected){
                try {
                    // Attente d'une connexion entrante
                    socket = serverSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "Erreur lors de l'acceptation de la connexion", e);
                }

                if (socket != null) {
                    System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                    // La connexion a été acceptée
                    // Vous pouvez utiliser le socket pour communiquer avec le périphérique distant

                    // Fermer le BluetoothServerSocket une fois la connexion établie
                    try {
                        serverSocket.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Erreur lors de la fermeture du BluetoothServerSocket", e);
                    }
                }
            }
        }

        public void cancel() {
            try {
                serverSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Erreur lors de la fermeture du BluetoothServerSocket", e);
            }
        }
    }




    private class ClientThread extends Thread {
        BluetoothDevice serverDevice;
        BluetoothSocket socket;

        @SuppressLint("MissingPermission")
        public ClientThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;
            serverDevice = bluetoothAdapter.getRemoteDevice(device.getAddress());

            try {
                // Création d'un socket BluetoothSocket pour se connecter au périphérique distant
                tmp = serverDevice.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Erreur lors de la création du BluetoothSocket", e);
            }
            socket = tmp;
        }

        @SuppressLint("MissingPermission")
        public void run() {
            // Annuler la découverte des périphériques Bluetooth

            bluetoothAdapter.cancelDiscovery();

            try {
                // Établir la connexion avec le périphérique distant
                socket.connect();
                isConnected = true;
            } catch (IOException e) {
                mHandler.obtainMessage(CONNECTING_STATUS, -1, -1)
                        .sendToTarget();
                Log.e(TAG, "Erreur lors de la connexion au périphérique distant", e);
                try {
                    socket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "Erreur lors de la fermeture du BluetoothSocket", e2);
                }
                return;
            }

            // La connexion a été établie
            // Vous pouvez utiliser le socket pour communiquer avec le périphérique distant
            if(isConnected){
                System.out.println(">>> Envoi données");

                    connectedThread = new ConnectedThread(socket);
                    connectedThread.start();

                    mHandler.obtainMessage(CONNECTING_STATUS, 1, -1)
                            .sendToTarget();
            }

        }

        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) {
                Log.e(TAG, "Erreur lors de la fermeture du BluetoothSocket", e);
            }
        }
    }




    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "ConnectedThread: Starting.");

            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run(){
            byte[] buffer = new byte[1024];  // buffer store for the stream

            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                // Read from the InputStream
                try {
                    bytes = mmInStream.read(buffer);
                    String incomingMessage = new String(buffer, 0, bytes);
                    Log.d(TAG, "InputStream: " + incomingMessage);
                } catch (IOException e) {
                    Log.e(TAG, "write: Error reading Input Stream. " + e.getMessage() );
                    break;
                }
            }
        }

        //Call this from the main activity to send data to the remote device
        public void write(byte[] bytes) {
            String text = new String(bytes, Charset.defaultCharset());
            Log.d(TAG, "write: Writing to outputstream: " + text);
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e(TAG, "write: Error writing to output stream. " + e.getMessage() );
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException ignored) { }
        }
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
        if(ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.BLUETOOTH_CONNECT)
                == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.BLUETOOTH_SCAN)
                        == PackageManager.PERMISSION_GRANTED){
            if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering()) {
                stopBluetoothDiscovery();
            }
        }

        // Si appareil connecté à un périphérique, rupture de la connexion
        if(selectedDevice != null){
            closeBluetoothConnection();
        }

        if (receiver != null) {
            requireActivity().unregisterReceiver(receiver);
            receiver = null;
        }

        binding = null;
    }
}