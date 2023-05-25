package com.example.slowchien.ui.exchange;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.UUID;

public class BluetoothUtils {

    private final BluetoothAdapter mBluetoothAdapter;
    private final ArrayList<String> mDeviceNames;
    private final ArrayList<BluetoothDevice> mDevices;
    private final ArrayAdapter<String> mAdapter;
    private final AlertDialog mAlertDialog;
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    // Durée de scan (en millisecondes)
    private static final long SCAN_PERIOD = 10000;

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

    @SuppressLint("MissingPermission")
    public BluetoothUtils(Context context) {

        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        mDeviceNames = new ArrayList<>();
        mDevices = new ArrayList<>();
        mAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, mDeviceNames);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Appareils disponibles");
        builder.setAdapter(mAdapter, null);
        builder.setPositiveButton("Fermer", null);
        builder.setAdapter(mAdapter, (dialog, which) -> {
            // Connexion au périphérique sélectionné
            BluetoothDevice device = mDevices.get(which);
            if(device.getName() != null){
                connectToDevice(device, context);
            } else {
                Toast.makeText(context, "Connexion impossible\nPériphérique inconnu", Toast.LENGTH_SHORT).show();
            }
        });
        mAlertDialog = builder.create();
    }

    @SuppressLint("MissingPermission")
    public void scanLeDevice(final boolean enable) {
        if (enable) {
            // Début du scan
            if (mDeviceNames.size() > 0){
                mDeviceNames.clear();
            }
            if (mDevices.size() > 0){
                mDevices.clear();
            }
            mAdapter.notifyDataSetChanged();
            mAlertDialog.show();
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            mHandler.postDelayed(() -> {
                // Fin du scan après SCAN_PERIOD millisecondes
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            }, SCAN_PERIOD);
        } else {
            // Arrêt du scan
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            mAlertDialog.dismiss();
        }
    }

    // Callback appelé lorsqu'un périphérique est détecté
    private final BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
            // Traitement effectué lorsqu'un périphérique est détecté
            mHandler.post(() -> {

                // Récupérer les informations contenues dans les services/UUID du périphérique
                @SuppressLint("MissingPermission") ParcelUuid[] tabUUIDs = device.getUuids();

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

                // Récupérer le nom du périphérique
                @SuppressLint("MissingPermission") String deviceName = device.getName();

                // Si le nom du périphérique est null, on utilise son adresse MAC pour tenter de trouver un nom
                if(deviceName == null){

                    String deviceAddress = device.getAddress();

                    // Utilisation de l'adresse MAC pour obtenir le nom du périphérique
                    BluetoothDevice namedDevice = mBluetoothAdapter.getRemoteDevice(deviceAddress);
                    deviceName = namedDevice.getName() != null ? "📱 " + namedDevice.getName() : "❔ Inconnu";

                } else {
                    deviceName = "📱 " + deviceName;
                }

                String deviceInfo = deviceName + "\n📌 " + device.getAddress(); //+ "\nRSSI: " + rssi;

                // Si nouveau périphérique identifié comme un téléphone mobile est trouvé, on l'ajoute à la liste

////////////////// Si vous ne voulez QUE les téléphones mobiles, utilisez la ligne suivante et commentez la ligne 152
                // if (!mDeviceNames.contains(deviceInfo) && deviceIsMobilePhone) { // ONLY MOBILES

////////////////// Si vous voulez TOUS les périphériques détectés, sans filtrer leur type, utilisez la ligne suivante et commentez la ligne 148
                if (!mDeviceNames.contains(deviceInfo)) { // ALL DEVICES

                    mDeviceNames.add(deviceInfo);
                    mDevices.add(device);
                    mAdapter.notifyDataSetChanged();
                }
            });
        }
    };

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


}
