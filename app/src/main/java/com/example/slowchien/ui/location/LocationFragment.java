package com.example.slowchien.ui.location;

import static com.example.slowchien.ui.location.JSONUtils.loadJSONFromFile;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.slowchien.R;
import com.example.slowchien.databinding.FragmentLocationBinding;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;

public class LocationFragment extends Fragment {

    private FragmentLocationBinding binding;
    private MapView mapView;
    private TextView messageTextView;
    private MyLocationNewOverlay myLocationOverlay;

    private static final int PERMISSION_REQUEST_CODE = 123; // Choisissez un nombre entier de votre choix
    private static final String JSON_DIRECTORY = "json";
    private static final String MARKERS_FILE = "markers.json";

    @SuppressLint("ClickableViewAccessibility")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        binding = FragmentLocationBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        initJSONFile();

        messageTextView = root.findViewById(R.id.messageTextView);
        mapView = root.findViewById(R.id.map_view);

        mapView.setOnTouchListener(new View.OnTouchListener() {

            private final Handler handler = new Handler();
            private Runnable runnable;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        runnable = () -> {
                            showPopupCreationMarker(event); // Appeler la méthode en passant le paramètre
                        };
                        handler.postDelayed(runnable, 1000);
                        break;
                    case MotionEvent.ACTION_UP:
                        handler.removeCallbacks(runnable);
                        break;
                }
                return false;
            }

        });

        return root;
    }

    private void showPopupCreationMarker(MotionEvent event) {

        Projection projection = mapView.getProjection();
        Point screenPoint = new Point((int) event.getX(), (int) event.getY());
        IGeoPoint geoPoint = projection.fromPixels(screenPoint.x, screenPoint.y);

        double latitude = geoPoint.getLatitude();
        double longitude = geoPoint.getLongitude();

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        // Créer un titre custom centré
        TextView titleTextView = new TextView(getContext());

        String title = "📮 Nouvelle Boîte aux Lettres"; // Contenu du titre
        SpannableString underlinedTitle = new SpannableString(title);
        underlinedTitle.setSpan(new UnderlineSpan(), 3, title.length(), 0); // Soulignement du texte

        titleTextView.setText(underlinedTitle); // Définition du texte
        titleTextView.setTextSize(20); // Taille du texte
        titleTextView.setGravity(Gravity.CENTER); // Position centrée du texte
        titleTextView.setPadding(0,64,0,0); // Espacement
        titleTextView.setTypeface(null, Typeface.BOLD); // Mise en gras du texte

        builder.setCustomTitle(titleTextView);

        // Création du LinearLayout contenant les TextInputLayout de titre et de description
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);

        // Initialisation du champ de saisie de titre
        TextInputLayout inputTitre = createTextInputLayout("📍 Titre du Marker", "Titre");
        layout.addView(inputTitre);

        // Initialisation du champ de saisie de description
        TextInputLayout inputDesc = createTextInputLayout("🔎 Description du Marker", "Description");
        layout.addView(inputDesc);

        // Ajouter le layout contenant les champs de saisie au AlertDialog
        builder.setView(layout);

        // Initialisation du bouton d'Ajout
        builder.setPositiveButton("Ajouter", (dialog, which) -> {
            // Récupérer les valeurs des inputs
            String textTitre = Objects.requireNonNull(inputTitre.getEditText()).getText().toString();
            String textDesc = Objects.requireNonNull(inputDesc.getEditText()).getText().toString();

            boolean isTitreEmpty = textTitre.trim().isEmpty();
            boolean isDescEmpty = textDesc.trim().isEmpty();

            if(!isTitreEmpty && !isDescEmpty){

                // Ajouter le marker
                addNewMarker(textTitre, textDesc, latitude, longitude);
            }
        });

        // Initialisation du bouton de Retour
        builder.setNegativeButton("Annuler", (dialog, which) -> dialog.cancel());

        builder.show();

    }

    private TextInputLayout createTextInputLayout(String label, String placeholder) {

        @SuppressLint("UseRequireInsteadOfGet")
        TextInputLayout inputLayout = new TextInputLayout(Objects.requireNonNull(getContext()));
        EditText editText = new EditText(getContext());

        // Mise du libellé en gras
        SpannableString hint = new SpannableString(label);
        hint.setSpan(new StyleSpan(Typeface.BOLD), 0, hint.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Définir le texte en gras dans le TextInputLayout
        inputLayout.setHint(hint);

        editText.setHint(placeholder);
        editText.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));

        // Définir l'espacement personnalisé pour le libellé
        int labelPaddingTop = 256; // Spécifiez l'espacement souhaité en pixels
        int labelPaddingBottom = 64; // Spécifiez l'espacement souhaité en pixels
        int labelPaddingStart = 32; // Spécifiez l'espacement souhaité en pixels
        int labelPaddingEnd = 32; // Spécifiez l'espacement souhaité en pixels

        editText.setPadding(
                labelPaddingStart,
                labelPaddingTop,
                labelPaddingEnd,
                labelPaddingBottom
        );

        inputLayout.addView(editText);

        return inputLayout;
    }

    public void addNewMarker(String titre, String desc, double latitude, double longitude){

        // Création du marqueur
        Marker marker = new Marker(mapView);
        marker.setPosition(new GeoPoint(latitude, longitude));
        marker.setTitle(titre);
        marker.setSnippet(desc);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM); // Ajustez l'ancre en fonction de vos besoins

        // Ajout du marqueur à la carte
        mapView.getOverlays().add(marker);

        // Actualisation de la carte pour afficher le marqueur
        mapView.invalidate();

        JSONUtils.ajouterValeurJSON(getContext(), MARKERS_FILE, latitude, longitude, titre, desc);
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

    @SuppressLint("SetTextI18n")
    private void showPermissionDeniedPage() {
        messageTextView.setText("Veuillez autoriser l'application à accéder à votre localisation.");
        messageTextView.setTextColor(getResources().getColor(android.R.color.black));
        messageTextView.setVisibility(View.VISIBLE);
    }

    public void initJSONFile(){

        try {
            // Récupération du fichier JSON contenu dans le répertoire assets
            InputStream inputStream = requireActivity().getAssets().open(MARKERS_FILE);
            String jsonString = new Scanner(inputStream).useDelimiter("\\A").next();

            // Copie du fichier JSON dans le stockage interne depuis le fichier assets
            JSONUtils.saveJsonFileToInternalStorage(getContext(), MARKERS_FILE, jsonString);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadMarkers(MapView mapView){

        // Chargement des markers depuis le fichier JSON
        ArrayList<MarkerMap> tabMarkers = new ArrayList<>();
        try {

            File directory = new File(getContext().getFilesDir(), JSON_DIRECTORY);
            File file = new File(directory, MARKERS_FILE);
            String jsonString = loadJSONFromFile(file.getAbsolutePath());

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
        } catch (JSONException e) {
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