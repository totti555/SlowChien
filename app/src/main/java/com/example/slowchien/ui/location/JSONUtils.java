package com.example.slowchien.ui.location;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class JSONUtils {

    private static final String TAG = JSONUtils.class.getSimpleName();
    private static final String JSON_DIRECTORY = "json";
    private static final String MARKERS_FILE = "markers.json";
    private static final String MESSAGES_FILE = "messages.json";
    private static final String CHAT_FILE = "chat.json";
    private static final String SENT_FILE = "sent.json";
    private static final String RECEIVED_FILE = "received.json";
    private static final String MY_MAC_ADDRESS = "FF-FF-FF-FF-FF-FF";



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

    public static void ajouterValeurJSON(Context context, String fileName, double latitude, double longitude, String titre, String desc) {

        switch (fileName) {
            case MARKERS_FILE:
                try {
                    // Récupération du fichier JSON existant depuis le stockage interne
                    File directory = new File(context.getFilesDir(), JSON_DIRECTORY);
                    File file = new File(directory, MARKERS_FILE);

                    // Chargement du fichier JSON
                    String jsonString = loadJSONFromFile(file.getAbsolutePath());

                    // Conversion de la chaîne JSON en un tableau JSON
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
                // TODO : Écriture/modification du fichier messages
                break;
            default:
                break;
        }
    }

    public static void créerChatJson(Context context) {
        try {
            String jsonDirectoryPath = context.getFilesDir().getAbsolutePath() + "/" + JSON_DIRECTORY;
            String filePath = jsonDirectoryPath + "/" + MESSAGES_FILE;
            String outputFilePath = jsonDirectoryPath + "/" + CHAT_FILE;

            // Chargement JSON
            String content = loadJSONFromFile(filePath);

            // Traitement JSON
            JSONArray messages = new JSONArray(content);
            JSONArray filteredMessages = new JSONArray();

            for (int i = 0; i < messages.length(); i++) {
                JSONObject message = messages.getJSONObject(i);
                String srcAddress = message.optString("macAddressSrc", "");
                String destAddress = message.optString("macAddressDest", "");

                if (srcAddress.equals(MY_MAC_ADDRESS) || destAddress.equals(MY_MAC_ADDRESS)) {
                    System.out.println(message);
                    filteredMessages.put(message);
                }
            }


            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath));
            writer.write(filteredMessages.toString(4)); // Indentation de 4 espaces pour une meilleure lisibilité
            writer.close();

            Log.d(TAG, "Filtrage terminé. Le fichier chat.json a été créé avec succès.");
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    public static void createSentReceiveJson(Context context, String inputFileName, String outputFileName, String filterKey) {
        try {
            String jsonDirectoryPath = context.getFilesDir().getAbsolutePath() + "/" + JSON_DIRECTORY;
            String inputFilePath = jsonDirectoryPath + "/" + inputFileName;
            String outputFilePath = jsonDirectoryPath + "/" + outputFileName;

            // Chargement JSON
            String content = loadJSONFromFile(inputFilePath);

            // Traitement JSON
            JSONArray jsonArray = new JSONArray(content);
            JSONArray filteredArray = new JSONArray();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String value = jsonObject.optString(filterKey, "");

                if (value.equals(MY_MAC_ADDRESS)) {
                    System.out.println(jsonObject);
                    filteredArray.put(jsonObject);
                }
            }

            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath));
            writer.write(filteredArray.toString(4)); // Indentation de 4 espaces pour une meilleure lisibilité
            writer.close();

            Log.d(TAG, "Filtrage terminé. Le fichier " + outputFileName + " a été créé avec succès.");
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    public static void cleanJSONFile(Context context, String fileName) {
        try {
            String jsonDirectoryPath = context.getFilesDir().getAbsolutePath() + "/" + JSON_DIRECTORY;
            String filePath = jsonDirectoryPath + "/" + fileName;

            JSONArray cleanedArray = new JSONArray(); // Crée un nouveau tableau JSON vide

            BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
            writer.write(cleanedArray.toString(4));
            writer.close();

            // Supprimer le fichier après l'avoir nettoyé
            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
            }

            Log.d(TAG, "Nettoyage terminé. Le fichier " + fileName + " a été nettoyé avec succès.");
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    public static void cleanAllJSONFiles(Context context) {
        JSONUtils.cleanJSONFile(context,MESSAGES_FILE);
        JSONUtils.cleanJSONFile(context,SENT_FILE);
        JSONUtils.cleanJSONFile(context,RECEIVED_FILE);
        JSONUtils.cleanJSONFile(context,CHAT_FILE);
    }



    public static void updateChatJson(Context context) {
        try {
            String jsonDirectoryPath = context.getFilesDir().getAbsolutePath() + "/" + JSON_DIRECTORY;
            String messagesFilePath = jsonDirectoryPath + "/" + MESSAGES_FILE;
            String chatFilePath = jsonDirectoryPath + "/" + CHAT_FILE;
            String sentFilePath = jsonDirectoryPath + "/" + SENT_FILE;

            // Charger les messages existants depuis messages.json
            String messagesContent = loadJSONFromFile(messagesFilePath);
            JSONArray messages = new JSONArray(messagesContent);

            // Charger les messages filtrés existants depuis chat.json
            String chatContent = loadJSONFromFile(chatFilePath);
            JSONArray filteredMessages = new JSONArray(chatContent);

            // Charger les messages envoyés existants depuis sent.json
            String sentContent = loadJSONFromFile(sentFilePath);
            JSONArray sentMessages = new JSONArray(sentContent);

            // Parcourir les nouveaux messages dans messages.json
            for (int i = 0; i < messages.length(); i++) {
                JSONObject message = messages.getJSONObject(i);
                String srcAddress = message.optString("macAddressSrc", "");

                // Vérifier si l'adresse source correspond à MY_MAC_ADDRESS
                if (srcAddress.equals(MY_MAC_ADDRESS)) {
                    // Vérifier si le message existe déjà dans le fichier chat.json
                    if (!containsMessage(filteredMessages, message)) {
                        System.out.println(message);
                        filteredMessages.put(message);
                    }
                    // Vérifier si le message existe déjà dans le fichier sent.json
                    if (!containsMessage(sentMessages, message)) {
                        sentMessages.put(message);
                    }
                }
            }

            // Écrire le tableau JSON des messages filtrés dans le fichier chat.json
            BufferedWriter chatWriter = new BufferedWriter(new FileWriter(chatFilePath));
            chatWriter.write(filteredMessages.toString(4)); // Indentation de 4 espaces pour une meilleure lisibilité
            chatWriter.close();

            // Écrire le tableau JSON des messages envoyés dans le fichier sent.json
            BufferedWriter sentWriter = new BufferedWriter(new FileWriter(sentFilePath));
            sentWriter.write(sentMessages.toString(4)); // Indentation de 4 espaces pour une meilleure lisibilité
            sentWriter.close();

            Log.d(TAG, "Mise à jour des fichiers chat.json et sent.json effectuée avec succès.");
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    private static boolean containsMessage(JSONArray jsonArray, JSONObject message) {
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject storedMessage = jsonArray.optJSONObject(i);
            if (storedMessage != null && storedMessage.toString().equals(message.toString())) {
                return true;
            }
        }
        return false;
    }


}