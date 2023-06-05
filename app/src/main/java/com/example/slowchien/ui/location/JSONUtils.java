package com.example.slowchien.ui.location;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.io.BufferedReader;
import java.io.FileOutputStream;

import java.io.FileReader;

public class JSONUtils {

    private static final String TAG = JSONUtils.class.getSimpleName();
    private static final String JSON_DIRECTORY = "json";
    private static final String MARKERS_FILE = "markers.json";
    private static final String MESSAGES_FILE = "messages.json";
    private static final String CONTACT_FILE = "contact.json";

    public static void saveJsonFileToInternalStorage(Context context, String fileName, String jsonData) {

        // Création du répertoire "json" dans le stockage interne
        File directory = new File(context.getFilesDir(), JSON_DIRECTORY);
        if (!directory.exists()) {
            directory.mkdir();
        }

        try {
            // Création du fichier JSON dans le répertoire "json"
            File file = new File(directory, fileName);
            if( !file.exists() ){
                FileWriter fileWriter = new FileWriter(file);
                fileWriter.write(jsonData);
                fileWriter.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void ajouterValeurJSON(Context context, String fileName, double latitude, double longitude, String titre, String desc) {

        switch( fileName ){
            case MARKERS_FILE:
                try {
                    // Récupération du fichier JSON existant depuis le stockage interne
                    File directory = new File(context.getFilesDir(), JSON_DIRECTORY);
                    File file = new File(directory, MARKERS_FILE);

                    // Chargement du fichier JSON
                    String jsonString = loadJSONFromFile(file.getAbsolutePath());

                    // Conversion la chaîne JSON en un tableau JSON
                    JSONArray jsonArray = new JSONArray(jsonString);

                    // Création du nouvel objet JSON
                    JSONObject nouvelObjet = new JSONObject();

                    nouvelObjet.put("latitude", latitude);
                    nouvelObjet.put("longitude", longitude);
                    nouvelObjet.put("titre", titre);
                    nouvelObjet.put("desc", desc);

                    // Ajout du nouvel objet au tableau JSON existant
                    jsonArray.put(nouvelObjet);

                    // Enregistrement du tableau JSON mis à jour dans le fichier
                    writeJSONToFile(file.getAbsolutePath(), jsonArray.toString());

                    Log.d(TAG, "Fichier JSON modifié avec succès.");

                } catch (JSONException e) {

                    Log.e(TAG, "Erreur lors de l'ajout de la valeur JSON : " + e.getMessage());
                    e.printStackTrace();
                }
                break;
            case MESSAGES_FILE:
                //TODO Ecriture/modification fichier messages
                break;
            default:
                break;
        }

    }

    public static String loadJSONFromFile(String filePath) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            bufferedReader.close();
        } catch (IOException e) {
            Log.e(TAG, "Erreur lors du chargement du fichier JSON : " + e.getMessage());
        }
        return stringBuilder.toString();
    }

    private static void writeJSONToFile(String filePath, String jsonString) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(filePath);
            fileOutputStream.write(jsonString.getBytes());
            fileOutputStream.close();
        } catch (IOException e) {
            Log.e(TAG, "Erreur lors de l'écriture du fichier JSON : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void ajouterValeurJSONContact(Context context, String contactFile, String macAddress, String name, String surname, String address, String description) {
        try {
            // Récupération du fichier JSON existant depuis le stockage interne
            File directory = new File(context.getFilesDir(), JSON_DIRECTORY);
            File file = new File(directory, CONTACT_FILE);

            // Chargement du fichier JSON
            String jsonString = loadJSONFromFile(file.getAbsolutePath());

            // Conversion la chaîne JSON en un tableau JSON
            JSONArray jsonArray = new JSONArray(jsonString);

            // Création du nouvel objet JSON
            JSONObject nouvelObjet = new JSONObject();

            nouvelObjet.put("macAddress", macAddress);
            nouvelObjet.put("name", name);
            nouvelObjet.put("surname", surname);
            nouvelObjet.put("adress", address);
            nouvelObjet.put("description", description);

            // Ajout du nouvel objet au tableau JSON existant
            jsonArray.put(nouvelObjet);

            // Enregistrement du tableau JSON mis à jour dans le fichier
            writeJSONToFile(file.getAbsolutePath(), jsonArray.toString());

            Log.d(TAG, "Fichier JSON modifié avec succès.");

        } catch (JSONException e) {

            Log.e(TAG, "Erreur lors de l'ajout de la valeur JSON : " + e.getMessage());
            e.printStackTrace();
        }


    }
}



