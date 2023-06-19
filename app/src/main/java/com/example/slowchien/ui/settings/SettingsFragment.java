package com.example.slowchien.ui.settings;

import static java.sql.DriverManager.println;

import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.slowchien.MainActivity;
import com.example.slowchien.R;
import com.example.slowchien.databinding.FragmentSettingsBinding;
import com.example.slowchien.ui.location.JSONUtils;
import com.google.android.gms.vision.text.Text;

import java.util.Set;

public class SettingsFragment extends Fragment {
    private FragmentSettingsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        SettingsViewModel settingsViewModel =
                new ViewModelProvider(this).get(SettingsViewModel.class);

        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textBtnBT = binding.ButtonCleanData;
        settingsViewModel.getCleanBtnLib().observe(getViewLifecycleOwner(), textBtnBT::setText);
        Button mScanButton = root.findViewById(R.id.ButtonCleanData);


        final TextView textBtnMac = binding.ChangeMacAdress;
        settingsViewModel.getChangeMACAdress().observe(getViewLifecycleOwner(), textBtnMac::setText);
        Button mScanButton2 = root.findViewById(R.id.ChangeMacAdress);

        final TextView textViewMACAddress = binding.textSettings;
        TextView textMacAdress=root.findViewById(R.id.text_settings);
        textMacAdress.setText("Adresse MAC :"+ MainActivity.getMacAddr(getContext()));
        settingsViewModel.getMACAddressText() .observe(getViewLifecycleOwner(), textViewMACAddress::setText);

        // Ajout d'un écouteur sur le bouton de scan
        // Utilisation dans la méthode onCreateView()
        mScanButton.setOnClickListener(v -> showConfirmationPopup(v));
        mScanButton2.setOnClickListener(v -> changeMacAddress(v,textMacAdress));
        return root;
    }



    // Fonction pour générer la popup de confirmation
    private void showConfirmationPopup(View anchorView) {
        Dialog dialog = new Dialog(requireContext(), R.style.RoundDialogWithPadding);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        int width = getResources().getDisplayMetrics().widthPixels;
        int dialogWidth = (int) (width * 0.9); // 90% de la largeur de l'écran
        int dialogHeight = (int) (getResources().getDisplayMetrics().heightPixels * 0.5); // 50% de la hauteur de l'écran

        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.width = dialogWidth;
            params.height = dialogHeight;
            window.setAttributes(params);
            window.setGravity(Gravity.CENTER); // Aligner le dialogue au centre
        }



        LinearLayout dialogLayout = new LinearLayout(requireContext());
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setGravity(Gravity.CENTER);
        dialog.setContentView(dialogLayout);

        TextView confirmMessage = new TextView(requireContext());
        confirmMessage.setText("Voulez-vous vraiment supprimer les données de contact et de message?");
        dialogLayout.addView(confirmMessage);

        LinearLayout buttonLayout = new LinearLayout(requireContext());
        buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
        buttonLayout.setGravity(Gravity.CENTER);
        dialogLayout.addView(buttonLayout);

        Button yesButton = createButton("Oui", v -> {
            dialog.dismiss();
            JSONUtils.cleanAllJSONFiles(getContext());
            showResultPopup(anchorView, true);
        });
        LinearLayout.LayoutParams yesButtonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        yesButtonParams.setMargins(0, 0, 16, 0); // Ajouter une marge à droite
        yesButton.setLayoutParams(yesButtonParams);
         buttonLayout.addView(yesButton);

        Button noButton = createButton("Non", v -> dialog.dismiss());
        buttonLayout.addView(noButton);

        dialog.show();
    }





    // Fonction pour générer la popup de résultat
    private void showResultPopup(View anchorView, boolean success) {
        Dialog dialog = new Dialog(requireContext(), R.style.RoundDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        int width = getResources().getDisplayMetrics().widthPixels;
        int dialogWidth = (int) (width * 0.9); // 90% de la largeur de l'écran
        int dialogHeight = (int) (getResources().getDisplayMetrics().heightPixels * 0.5); // 50% de la hauteur de l'écran

        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.width = dialogWidth;
            params.height = dialogHeight;
            window.setAttributes(params);
            window.setGravity(Gravity.CENTER); // Aligner le dialogue au centre
        }

        LinearLayout dialogLayout = new LinearLayout(requireContext());
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setGravity(Gravity.CENTER);
        dialog.setContentView(dialogLayout);

        TextView resultMessage = new TextView(requireContext());
        if (success) {
            resultMessage.setText("Fichiers nettoyés avec succès");
        } else {
            resultMessage.setText("Échec du nettoyage des fichiers");
        }
        dialogLayout.addView(resultMessage);

        LinearLayout buttonLayout = new LinearLayout(requireContext());
        buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
        buttonLayout.setGravity(Gravity.CENTER);
        dialogLayout.addView(buttonLayout);

        Button closeButton = createButton("Fermer", v -> dialog.dismiss());
        buttonLayout.addView(closeButton);

        dialog.show();
    }

    public void changeMacAddress(View anchorView,TextView textMacAdress) {
        Dialog dialog = new Dialog(requireContext(), R.style.RoundDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        int width = getResources().getDisplayMetrics().widthPixels;
        int dialogWidth = (int) (width * 0.9); // 90% de la largeur de l'écran

        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.width = dialogWidth;
            window.setAttributes(params);
            window.setGravity(Gravity.CENTER); // Aligner le dialogue au centre
        }

        LinearLayout dialogLayout = new LinearLayout(requireContext());
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setGravity(Gravity.CENTER);
        dialog.setContentView(dialogLayout);

        EditText macAddressEditText = new EditText(requireContext());
        macAddressEditText.setText(MainActivity.getMacAddr(getContext())); // Valeur par défaut
        macAddressEditText.setHint("Adresse MAC");
        dialogLayout.addView(macAddressEditText);

        LinearLayout buttonLayout = new LinearLayout(requireContext());
        buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
        buttonLayout.setGravity(Gravity.CENTER);
        dialogLayout.addView(buttonLayout);

        Button validateButton = createButton("Valider", v -> {
            String macAddress = macAddressEditText.getText().toString();
            // Faites quelque chose avec l'adresse MAC saisie
            MainActivity.saveMacAddress(getContext(),macAddress);
            textMacAdress.setText("Adresse MAC :"+ MainActivity.getMacAddr(getContext()));
            dialog.dismiss();
        });
        buttonLayout.addView(validateButton);
        dialog.show();
    }


    // Fonction pour créer un bouton avec un texte, une couleur de fond et un écouteur
    private Button createButton(String buttonText, View.OnClickListener listener) {
        Button button = new Button(getActivity());
        button.setText(buttonText);
        button.setBackgroundColor(getResources().getColor(R.color.purple_500)); // Couleur de fond violette
        button.setOnClickListener(listener);
        return button;
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
