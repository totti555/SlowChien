package com.example.slowchien.ui.exchange;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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


public class ExchangeFragment extends Fragment {

    private FragmentExchangeBinding binding;

    private BluetoothUtils mBluetoothUtils;

    private static final int REQUEST_ENABLE_BT = 1; //ou 456
    private static final int PERMISSION_REQUEST_FINE_LOCATION = 100;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        ExchangeViewModel exchangeViewModel =
                new ViewModelProvider(this).get(ExchangeViewModel.class);

        binding = FragmentExchangeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        //  Match to the id of the exchange layout
        final TextView textView = binding.textExchange;
        exchangeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        final TextView textBtnBT = binding.findBluetoothPeriph;
        exchangeViewModel.getBluetoothBtnLib().observe(getViewLifecycleOwner(), textBtnBT::setText);

        // Récupération de la référence à l'objet BluetoothUtils
        mBluetoothUtils = new BluetoothUtils(requireActivity());

        // Récupération de la référence au bouton de scan
        Button mScanButton = root.findViewById(R.id.findBluetoothPeriph);

        // Ajout d'un écouteur sur le bouton de scan
        mScanButton.setOnClickListener(v -> {

            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

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

            // Démarrage du scan
            Toast.makeText(getActivity(), "Scan en cours...", Toast.LENGTH_SHORT).show();
            mBluetoothUtils.scanLeDevice(true);
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}