package com.example.slowchien.ui.settings;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.slowchien.R;
import com.example.slowchien.databinding.FragmentSettingsBinding;
import com.example.slowchien.ui.location.JSONUtils;

import java.util.Set;

public class SettingsFragment extends Fragment {
    private FragmentSettingsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        SettingsViewModel settingsViewModel =
                new ViewModelProvider(this).get(SettingsViewModel.class);

        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textSettings;
        settingsViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        final TextView textBtnBT = binding.ButtonCleanData;
        settingsViewModel.getCleanBtnLib().observe(getViewLifecycleOwner(), textBtnBT::setText);
        Button mScanButton = root.findViewById(R.id.ButtonCleanData);

        // Ajout d'un écouteur sur le bouton de scan
        // Utilisation dans la méthode onCreateView()
        mScanButton.setOnClickListener(v -> showConfirmationPopup(v));

        return root;
    }



    // Fonction pour générer la popup de confirmation
    private void showConfirmationPopup(View anchorView) {
        PopupWindow popupWindow = new PopupWindow(getActivity());
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(false);

        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        LinearLayout popupLayout = new LinearLayout(getActivity());
        popupLayout.setLayoutParams(layoutParams);
        popupLayout.setOrientation(LinearLayout.VERTICAL);
        popupLayout.setGravity(Gravity.CENTER);


        TextView confirmMessage = new TextView(getActivity());
        confirmMessage.setText("Voulez-vous vraiment supprimer les données de contact et de message?");
        popupLayout.addView(confirmMessage);

        LinearLayout buttonLayout = new LinearLayout(getActivity());
        buttonLayout.setLayoutParams(layoutParams);
        buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
        buttonLayout.setGravity(Gravity.CENTER);

        Button yesButton = createButton("Oui", v -> {
            popupWindow.dismiss();
            JSONUtils.cleanAllJSONFiles(getContext());
            showResultPopup(anchorView, true);//todo gestion d'erreurs
        });
        buttonLayout.addView(yesButton);

        Button noButton = createButton("Non", v -> popupWindow.dismiss());
        buttonLayout.addView(noButton);

        popupLayout.addView(buttonLayout);

        popupWindow.setContentView(popupLayout);
        popupWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.showAtLocation(anchorView, Gravity.CENTER, 0, 0);
    }

    // Fonction pour générer la popup de résultat
    private void showResultPopup(View anchorView, boolean success) {
        PopupWindow popupWindow = new PopupWindow(getActivity());
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(false);

        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        LinearLayout popupLayout = new LinearLayout(getActivity());
        popupLayout.setLayoutParams(layoutParams);
        popupLayout.setOrientation(LinearLayout.VERTICAL);
        popupLayout.setGravity(Gravity.CENTER);

        TextView resultMessage = new TextView(getActivity());
        if (success) {
            resultMessage.setText("Fichiers nettoyés avec succès");
        } else {
            resultMessage.setText("Échec du nettoyage des fichiers");
        }
        popupLayout.addView(resultMessage);

        Button closeButton = createButton("Fermer", v -> popupWindow.dismiss());
        popupLayout.addView(closeButton);

        popupWindow.setContentView(popupLayout);
        popupWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.showAtLocation(anchorView, Gravity.CENTER, 0, 0);
    }

    // Fonction pour créer un bouton avec un texte, une couleur de fond et un écouteur
    private Button createButton(String buttonText, View.OnClickListener listener) {
        Button button = new Button(getActivity());
        button.setText(buttonText);
        button.setOnClickListener(listener);
        return button;
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
