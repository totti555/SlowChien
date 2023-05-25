package com.example.slowchien.ui.location;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.slowchien.R;
import com.example.slowchien.databinding.FragmentLocationBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

public class LocationFragment extends Fragment {

    private FragmentLocationBinding binding;
    private MapView mapView;
    private TextView messageTextView;
    private MyLocationNewOverlay myLocationOverlay;

    private static final int PERMISSION_REQUEST_CODE = 123; // Choisissez un nombre entier de votre choix

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        binding = FragmentLocationBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        messageTextView = root.findViewById(R.id.messageTextView);
        mapView = root.findViewById(R.id.map_view);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Configuration d'osmdroid
        Configuration.getInstance().load(this.getContext(),
                PreferenceManager.getDefaultSharedPreferences(this.getContext()));

        // Si permissions non accordées
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

            // On demande à l'utilisateur de les accepter
            String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
            requestPermissions(permissions, PERMISSION_REQUEST_CODE);

        } else { // Si les permissions sont déjà accordées

            // On configure et affiche la mapView
            setupMap();

            // On charge ensuite les markers
            loadMarkers(mapView);

        }
    }

    private void setupMap() {

        // Setup de la mapView
        mapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
        mapView.setMultiTouchControls(true);

        // Ajouter une couche pour afficher la position actuelle
        myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(requireContext()), mapView);
        mapView.getOverlays().add(myLocationOverlay);
        myLocationOverlay.enableMyLocation();

        // Attendez un court instant avant de récupérer la position
        new Handler().postDelayed(() -> {
            // Obtenez la dernière position connue (latitude et longitude)
            if (myLocationOverlay.getMyLocation() != null) {

                // Centrez la carte sur une position spécifique
                mapView.getController().setZoom(16.0);
                mapView.getController().setCenter(new GeoPoint(myLocationOverlay.getMyLocation().getLatitude(), myLocationOverlay.getMyLocation().getLongitude()));

            } else {

                // Centrez la carte sur une position par défaut
                mapView.getController().setZoom(12.0);
                mapView.getController().setCenter(new GeoPoint(48.85656654552703, 2.3520098484730387)); // coordonnées par défaut de Paris
            }
            mapView.setMinZoomLevel(5.0);
        }, 1000); // Attendre 0.5 seconde (500 millisecondes) avant de récupérer la position
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) { // L'utilisateur a accordé les permissions

                // Configuration et affichage de la mapView
                setupMap();

                // Chargement des markers
                loadMarkers(mapView);

            } else { // L'utilisateur a refusé les permissions

                // Affichage du message demandant à l'utilisateur d'accorder les permissions
                showPermissionDeniedPage();
            }
        }
    }

    private void showPermissionDeniedPage() {
        messageTextView.setText("Veuillez autoriser l'application à accéder à votre localisation.");
        messageTextView.setTextColor(getResources().getColor(android.R.color.black));
        messageTextView.setVisibility(View.VISIBLE);
    }

    public void loadMarkers(MapView mapView){

        // Chargement des markers depuis le fichier JSON
        ArrayList<MarkerMap> tabMarkers = new ArrayList<>();
        try {
            InputStream inputStream = requireActivity().getAssets().open("markers.json");
            String jsonString = new Scanner(inputStream).useDelimiter("\\A").next();
            JSONArray jsonArray = new JSONArray(jsonString);
            System.out.println("JSON :" + jsonArray);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                double latitude = jsonObject.getDouble("latitude");
                double longitude = jsonObject.getDouble("longitude");
                String titreStr = jsonObject.getString("titre");
                String descStr = jsonObject.getString("desc");

                System.out.println(new MarkerMap(latitude, longitude, titreStr, descStr));
                tabMarkers.add(new MarkerMap(latitude, longitude, titreStr, descStr));
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            tabMarkers.forEach(m -> {

                // Création du marqueur
                Marker marker = new Marker(mapView);
                marker.setPosition(new GeoPoint(m.getLatitude(), m.getLongitude()));
                marker.setTitle(m.getTitre());
                marker.setSnippet(m.getDesc());
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM); // Ajustez l'ancre en fonction de vos besoins


                // Ajout du marqueur à la carte
                mapView.getOverlays().add(marker);

                // Actualisation de la carte pour afficher le marqueur
                mapView.invalidate();

            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}