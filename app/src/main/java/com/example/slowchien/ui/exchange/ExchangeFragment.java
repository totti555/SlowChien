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
import android.util.Log;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

        // Initialisation du bouton de déconnexion des téléphones
        final TextView textBtnSend = binding.sendFile;
        exchangeViewModel.getSendingBtnLib().observe(getViewLifecycleOwner(), textBtnSend::setText);

        // Récupération de la référence au bouton de scan
        mScanButton = root.findViewById(R.id.findBluetoothPeriph);

        // Récupération de la référence au bouton de détection
        mVisibilityButton = root.findViewById(R.id.setDeviceVisibility);
        mVisibilityButton.setEnabled(true);

        mDisconnectButton = root.findViewById(R.id.disconnectDevices);
        mDisconnectButton.setVisibility(View.GONE);

        mSendingButton = root.findViewById(R.id.sendFile);
        mSendingButton.setVisibility(View.GONE);

        // Vérification de la configuration Bluettoth de l'appareil
        checkBTConfig();

        // Initialisation du AlertDialog
        setupAlertDialog();

        // Ajout d'un écouteur sur le bouton de scan
        mScanButton.setOnClickListener(v -> checkScanPermissions());

        // Ajout d'un écouteur sur le bouton de visibilité
        mVisibilityButton.setOnClickListener(v -> checkVisibilityPermissions());

        mDisconnectButton.setOnClickListener(v -> closeBluetoothConnection());

        mSendingButton.setOnClickListener(v -> {
            try {
                sendJSONFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        return root;
    }

    public void checkBTConfig(){

        if (bluetoothAdapter == null) {
            // Le dispositif ne prend pas en charge Bluetooth
            Toast.makeText(getContext(), "Périphérique ne supportant pas le Bluetooth", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            // Le Bluetooth n'est pas activé, demander à l'utilisateur de l'activer
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
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

        // Vérification des permissions BLUETOOTH_CONNECT et BLUETOOTH_SCAN
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

                    String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH_ADVERTISE};
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
                                startBluetoothDiscovery();
                            } else {
                                // La permission est refusée, afficher un message ou prendre une autre action
                                Toast.makeText(requireContext(), "Permission Scan Bluetooth refusée", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            if (grantResults.length > 0 && grantResults[0]
                                    == PackageManager.PERMISSION_GRANTED) {
                                // Permission accordée, lancement du scan
                                startBluetoothDiscovery();
                            } else {
                                // La permission est refusée, afficher un message ou prendre une autre action
                                Toast.makeText(requireContext(), "Permission Scan Bluetooth refusée", Toast.LENGTH_SHORT).show();
                            }
                        }
                        break;
                    }
                }
            case PERMISSION_VISI_REQUEST:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                                // Permission accordée, lancement du scan
                                setupDeciveVisibility();
                            }
                        } else {
                            if (grantResults.length > 0 && grantResults[0]
                                    == PackageManager.PERMISSION_GRANTED) {
                                // Permission accordée, lancement du scan
                                setupDeciveVisibility();
                            }
                        }
                        break;
                    }
                }
            default:
                break;
        }
    }


    @SuppressLint("MissingPermission")
    private void startBluetoothDiscovery() {

        // Vérification des permissions Bluetooth
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

    private void setupDeciveVisibility(){
        int requestCode = 1;
        int tpsVisiSecondes = 60;
        int tpsVisiMillisecondes = tpsVisiSecondes * 1000;

        // Vérification des permissions Bluetooth
        mVisibilityButton.setEnabled(false);

        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, tpsVisiSecondes);

        startActivityForResult(discoverableIntent, requestCode);

        new Handler().postDelayed(() -> mVisibilityButton.setEnabled(true), tpsVisiMillisecondes);

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
                System.out.println(">>>>> Connexion à : " + device.getName() + " - " + device.getAddress());
                selectedDevice = device;
                if (selectedDevice.getBondState() != BluetoothDevice.BOND_BONDED) {
                    selectedDevice.createBond();
                } else {
                    Toast.makeText(this.getContext(), "Périphérique déjà connecté !", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this.getContext(), "Connexion impossible\nPériphérique inconnu", Toast.LENGTH_SHORT).show();
            }
        });
        mAlertDialog = builder.create();
    }

    @SuppressLint("MissingPermission")
    private void connectToSelectedDevice() {
        // Initialisez une connexion Bluetooth avec le périphérique ici
        // Par exemple, vous pouvez utiliser la classe BluetoothSocket pour établir une connexion sécurisée

        // Exemple de code pour établir une connexion sécurisée avec le périphérique
        try {

            socket = selectedDevice.createRfcommSocketToServiceRecord(MY_UUID);

            Toast.makeText(this.getContext(), "Connexion établie !", Toast.LENGTH_SHORT).show();

            mScanButton.setVisibility(View.GONE);
            mVisibilityButton.setVisibility(View.GONE);
            mDisconnectButton.setVisibility(View.VISIBLE);
            mSendingButton.setVisibility(View.VISIBLE);

            socket.connect();

            // La connexion a été établie avec succès, vous pouvez maintenant interagir avec le périphérique
        } catch (IOException e) {
            // Une erreur s'est produite lors de la connexion, vous pouvez gérer cette situation si nécessaire
        }
    }

    @SuppressLint("MissingPermission")
    private void closeBluetoothConnection(){
        try {
            if(selectedDevice != null){
                // Fermer la connexion BluetoothSocket
                socket.close();

                // Vérifier si le périphérique est actuellement associé (appairé)
                if (selectedDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                    // Dissocier le périphérique
                    try {
                        Method method = selectedDevice.getClass().getMethod("removeBond");
                        method.invoke(selectedDevice);

                        Log.d("Disconnect", "Device unpaired successfully");
                        Toast.makeText(requireContext(), "Déconnexion réussie !", Toast.LENGTH_SHORT).show();

                        mScanButton.setVisibility(View.VISIBLE);
                        mVisibilityButton.setVisibility(View.VISIBLE);
                        mDisconnectButton.setVisibility(View.GONE);
                        mSendingButton.setVisibility(View.GONE);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    // Le périphérique n'est pas associé (appairé)
                    Log.d("Disconnect", "Device is not bonded");
                }
            }
        } catch (IOException e) {
            // Gérer les erreurs éventuelles
            e.printStackTrace();
        }
    }

    private void sendJSONFile() throws IOException {

        Toast.makeText(requireContext(), "Envoi des données en cours...", Toast.LENGTH_SHORT).show();

        // Récupération du fichier JSON contenu dans le répertoire assets
        InputStream inputStream = requireActivity().getAssets().open(MARKERS_FILE);
        String jsonString = new Scanner(inputStream).useDelimiter("\\A").next();

        saveJsonToFile(requireContext(), jsonString, MARKERS_FILE);
    }

    public void saveJsonToFile(Context context, String jsonContent, String originalFileName) {
        try {
            // Obtention du répertoire de fichiers internes
            File filesDir = context.getFilesDir();

            // Création du nouveau nom de fichier
            String newFileName = "new_" + originalFileName;
            File newFile = new File(filesDir, newFileName);

            // Création du flux de sortie
            OutputStream outputStream = new FileOutputStream(newFile);

            // Conversion du JSON en tableau de bytes
            byte[] jsonBytes = jsonContent.getBytes();

            // Écriture du contenu dans le fichier
            outputStream.write(jsonBytes);
            Toast.makeText(requireContext(), "Données envoyées avec succès !", Toast.LENGTH_SHORT).show();

            // Fermeture du flux de sortie
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            // Gérer les exceptions ou afficher un message d'erreur
        }
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

                        // Vérifiez si le périphérique est un téléphone mobile
                        // Le périphérique détecté est un téléphone/téblette mobile

                        String deviceName = device.getName(); // Nom du périphérique
                        String deviceHardwareAddress = device.getAddress(); // Adresse MAC du périphérique

                        if(deviceName == null){
                            deviceName = "❔ Inconnu";
                        } else if (deviceIsMobilePhone) {
                            deviceName = "📱 " + device.getName();
                        } else {
                            deviceName = "❔ " + device.getName();
                        }
                        String deviceInfo = deviceName + "\n📌 " + deviceHardwareAddress; //+ "\nRSSI: " + rssi;

                        // Si nouveau périphérique identifié comme un téléphone mobile est trouvé, on l'ajoute à la liste
                        if (!mDeviceNames.contains(deviceInfo) && deviceIsMobilePhone) { // ONLY MOBILES

                            mDeviceNames.add(deviceInfo);
                            mDevices.add(device);
                            mAdapter.notifyDataSetChanged();
                        }
                        break;
                    case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
                        BluetoothDevice bondedDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);

                        if (bondedDevice.equals(selectedDevice)) {
                            if (bondState == BluetoothDevice.BOND_BONDED) {
                                // Le périphérique est maintenant appairé, vous pouvez continuer la connexion
                                // Ici, vous pouvez appeler une méthode pour établir la connexion avec le périphérique
                                connectToSelectedDevice();
                            }  // L'appairage a échoué, vous pouvez gérer cette situation si nécessaire

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

        // Unregister the ACTION_FOUND receiver.
        if (receiver != null) {
            requireActivity().unregisterReceiver(receiver);
            receiver = null;
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Arrêter la recherche de périphériques Bluetooth
        if(ContextCompat.checkSelfPermission(this.requireContext(), Manifest.permission.BLUETOOTH_CONNECT)
                == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(this.requireContext(), Manifest.permission.BLUETOOTH_SCAN)
                        == PackageManager.PERMISSION_GRANTED){
            if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering()) {
                stopBluetoothDiscovery();
            }
        }

        if(selectedDevice != null){
            closeBluetoothConnection();
        }

        binding = null;
    }
}