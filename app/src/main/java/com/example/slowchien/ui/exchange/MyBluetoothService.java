package com.example.slowchien.ui.exchange;

import static android.Manifest.permission.BLUETOOTH;
import static android.Manifest.permission.BLUETOOTH_CONNECT;
import static android.Manifest.permission.BLUETOOTH_SCAN;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class MyBluetoothService {
    public static final String TAG = "MyBluetoothService";
    private static final String NAME = "SlowChien";
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public static final int STATE_NONE = 0;
    public static final int STATE_LISTEN = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECTED = 3;

    public static final int REQUEST_BLUETOOTH = 100;
    public static final int REQUEST_BLUETOOTH_DISCOVERABLE = 101;

    private final Activity activity;
    private final Handler handler;
    private final BluetoothAdapter adapter;
    private int state;

    private AcceptThread secureAcceptThread;
    private AcceptThread insecureAcceptThread;
    private ConnectThread connectThread;
    private ConnectedThread connectedThread;

    public MyBluetoothService(Activity activity, Handler handler) {
        this.activity = activity;
        this.handler = handler;

        adapter = BluetoothAdapter.getDefaultAdapter();
        state = STATE_NONE;
    }

    public synchronized void start() {
        System.out.println(">>> MyBluetoothService : start() -> Création AcceptThreads");
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }

        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }

        if (secureAcceptThread == null) {
            secureAcceptThread = new AcceptThread();
            secureAcceptThread.start();
        }

        if (insecureAcceptThread == null) {
            insecureAcceptThread = new AcceptThread();
            insecureAcceptThread.start();
        }
    }

    public synchronized void connect(BluetoothDevice device) {
        System.out.println(">>> MyBluetoothService : connect() -> Création ConnectThread");
        if (state == STATE_CONNECTING) {
            if (connectThread != null) {
                connectThread.cancel();
                connectThread = null;
            }
        }

        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }

        connectThread = new ConnectThread(device);
        connectThread.start();
    }

    @SuppressLint("MissingPermission")
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        System.out.println(">>> MyBluetoothService : connected() -> Création ConnectedThread");
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }

        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }

        if (secureAcceptThread != null) {
            secureAcceptThread.cancel();
            secureAcceptThread = null;
        }

        if (insecureAcceptThread != null) {
            insecureAcceptThread.cancel();
            insecureAcceptThread = null;
        }

        connectedThread = new ConnectedThread(socket);
        connectedThread.start();
    }

    public synchronized void stop() {
        System.out.println(">>> MyBluetoothService : stop() -> Arrêt Threads");
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }

        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }

        if (secureAcceptThread != null) {
            secureAcceptThread.cancel();
            secureAcceptThread = null;
        }

        if (insecureAcceptThread != null) {
            insecureAcceptThread.cancel();
            insecureAcceptThread = null;
        }

        state = STATE_NONE;
    }

    public void write(byte[] bytes) {
        System.out.println(">>> MyBluetoothService : write() -> Ecriture dans ConnectedThread");
        ConnectedThread connectedThread1;

        synchronized (this) {
            if (state != STATE_CONNECTED) {
                return;
            }

            connectedThread1 = connectedThread;
        }

        connectedThread1.write(bytes);
    }

    public void sendData() {
        System.out.println(">>> MyBluetoothService : sendData() -> skipped");
        /*
        List<MessageModel> messages = getServiceMessages();
        List<LetterBoxModel> letterBoxes = LetterBoxViewModel.getLetterBoxes();
        List<ReceiptModel> receipts = getServiceReceipts();
        BluetoothModel bluetoothModel = new BluetoothModel(messages, letterBoxes, receipts);

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        try {
            ObjectOutputStream out = new ObjectOutputStream(bytes);
            out.writeObject(bluetoothModel);
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        write(bytes.toByteArray());
        */
    }

    private void connectionFailed() {
        System.out.println("Unable to connect");

        state = STATE_NONE;

        MyBluetoothService.this.start();
    }

    private void connectionLost() {
        System.out.println("Device connection was lost");

        state = STATE_NONE;

        MyBluetoothService.this.start();
    }

    @SuppressLint("MissingPermission")
    private class AcceptThread extends Thread {
        private final BluetoothServerSocket serverSocket;

        public AcceptThread() {
            System.out.println(">>> MyBluetoothService : AcceptThread() -> Initialisation AcceptThread");
            BluetoothServerSocket serverSocket1 = null;

            try {
                System.out.println(">>> MyBluetoothService : AcceptThread() -> Socket Listener");
                serverSocket1 = adapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket listen() failed", e);
            }

            serverSocket = serverSocket1;
            state = STATE_LISTEN;
        }

        @Override
        public void run() {
            setName("AcceptThread");
            BluetoothSocket socket;

            while (state != STATE_CONNECTED) {
                try {
                    System.out.println(">>> MyBluetoothService : AcceptThread - run() -> Socket accept");
                    socket = serverSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "Socket accept() failed", e);
                    break;
                }

                if (socket != null) {
                    synchronized (MyBluetoothService.this) {
                        switch (state) {
                            case STATE_LISTEN:
                            case STATE_CONNECTING:
                                System.out.println(">>> MyBluetoothService : AcceptThread - run() -> STATE_LISTEN / STATE_CONNECTING");
                                connected(socket, socket.getRemoteDevice());
                                break;
                            case STATE_NONE:
                            case STATE_CONNECTED:
                                try {
                                    System.out.println(">>> MyBluetoothService : AcceptThread - run() -> STATE_NONE / STATE_CONNECTED");
                                    socket.close();
                                } catch (IOException e) {
                                    Log.e(TAG, "Could not close unwanted socket", e);
                                }

                                break;
                        }
                    }
                }
            }
        }

        public void cancel() {
            try {
                System.out.println(">>> MyBluetoothService : AcceptThread - cancel() -> Close ServerSocket");
                serverSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Socket close() of server failed", e);
            }
        }
    }

    @SuppressLint("MissingPermission")
    private class ConnectThread extends Thread {
        private final BluetoothSocket socket;
        private final BluetoothDevice device;

        public ConnectThread(BluetoothDevice device) {
            System.out.println(">>> MyBluetoothService : ConnectThread() -> Initialisation ConnectThread");
            this.device = device;
            BluetoothSocket socket1 = null;

            try {
                System.out.println(">>> MyBluetoothService : ConnectThread() -> Create Socket");
                socket1 = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket create() failed", e);
            }

            socket = socket1;
            state = STATE_CONNECTING;
        }

        public void run() {
            setName("ConnectThread");

            System.out.println(">>> MyBluetoothService : ConnectThread() - run() -> Cancel Discovery");
            adapter.cancelDiscovery();

            try {
                System.out.println(">>> MyBluetoothService : ConnectThread() - run() -> Socket Connection");
                socket.connect();
            } catch (IOException e) {
                try {
                    System.out.println(">>> MyBluetoothService : ConnectThread() - run() -> Close Socket");
                    socket.close();
                } catch (IOException ex) {
                    Log.e(TAG, "unable to close() socket during connection failure", ex);
                }

                connectionFailed();
                return;
            }

            synchronized (MyBluetoothService.this) {
                connectThread = null;
            }

            System.out.println(">>> MyBluetoothService : ConnectThread() - run() -> Connected !");
            connected(socket, device);
        }

        public void cancel() {
            try {
                System.out.println(">>> MyBluetoothService : ConnectThread() - cancel() -> Close Socket");
                socket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket socket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public ConnectedThread(BluetoothSocket socket) {
            System.out.println(">>> MyBluetoothService : ConnectedThread() -> Initialisation ConnectedThread");
            this.socket = socket;

            InputStream inputStream1 = null;
            OutputStream outputStream1 = null;

            try {
                inputStream1 = socket.getInputStream();
                outputStream1 = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            inputStream = inputStream1;
            outputStream = outputStream1;
            state = STATE_CONNECTED;
        }

        public void run() {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            while (state == STATE_CONNECTED) {
                System.out.println(">>> MyBluetoothService : ConnectedThread() - run() -> STATE_CONNECTED");
                try {
                    if (inputStream.available() > 0) {
                        byteArrayOutputStream.write(inputStream.read());
                    } else {
                        if (byteArrayOutputStream.size() > 0) {
                            byte[] bytes = byteArrayOutputStream.toByteArray();
                            ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(bytes));

                            Object bluetoothModel = objectInputStream.readObject();
                            System.out.println("Message reçu");

                            byteArrayOutputStream.reset();
                        }
                    }
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost();
                    break;
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        public void write(byte[] bytes) {
            try {
                outputStream.write(bytes);
                System.out.println("Start writing");
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    public boolean isBluetoothEnabled() {
        return adapter.isEnabled();
    }

    public boolean isBluetoothAllowed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ContextCompat.checkSelfPermission(activity, BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED;
        }

        return ContextCompat.checkSelfPermission(activity, BLUETOOTH) == PackageManager.PERMISSION_GRANTED;
    }

    public boolean isBluetoothScanAllowed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ContextCompat.checkSelfPermission(activity, BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED;
        }

        return ContextCompat.checkSelfPermission(activity, BLUETOOTH) == PackageManager.PERMISSION_GRANTED;
    }

    public Set<BluetoothDevice> getPairedDevices() {
        if (ContextCompat.checkSelfPermission(activity, BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ActivityCompat.requestPermissions(activity, new String[]{BLUETOOTH_CONNECT}, REQUEST_BLUETOOTH);
                return new HashSet<>();
            }
        }

        return adapter.getBondedDevices();
    }

    public void startDiscovery() {
        if (ActivityCompat.checkSelfPermission(activity, BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ActivityCompat.requestPermissions(activity, new String[]{BLUETOOTH_SCAN}, REQUEST_BLUETOOTH_DISCOVERABLE);
                return;
            }
        }

        adapter.startDiscovery();
    }

    public void cancelDiscovery() {
        if (ActivityCompat.checkSelfPermission(activity, BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
            adapter.cancelDiscovery();
        }
    }
}

