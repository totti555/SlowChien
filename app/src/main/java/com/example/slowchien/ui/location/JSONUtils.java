package com.example.slowchien.ui.location;

import static com.example.slowchien.MainActivity.getCurrentDateTime;

import android.content.Context;
import android.util.Log;

import com.example.slowchien.MainActivity;
import com.example.slowchien.ui.home.Message;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;



public class JSONUtils {

    private static final String TAG = JSONUtils.class.getSimpleName();
    private static final String JSON_DIRECTORY = "json";
    private static final String MARKERS_FILE = "markers.json";
    private static final String MESSAGES_FILE = "messages.json";
    private static final String CHAT_FILE = "chat.json";
    private static final String SENT_FILE = "sent.json";
    private static final String CONTACTS_FILE = "contacts.json";
    private static final String RECEIVED_FILE = "received.json";
    public static String MY_MAC_ADDRESS = MainActivity.getMacAddr();




    public static void saveJsonFileToInternalStorage(Context context, String fileName, String jsonData) {

        // Création du répertoire "json" dans le stockage interne
        File directory = new File(context.getFilesDir(), JSON_DIRECTORY);

        if (!directory.exists()) {
            directory.mkdir();
        }

        File file = new File(directory, fileName);

        try {
            // Création du fichier JSON dans le répertoire "json"
            if( !file.exists()){
                FileWriter fileWriter = new FileWriter(file);
                fileWriter.write(jsonData);
                fileWriter.close();
            } else {
                String jsonString = JSONUtils.loadJSONFromFile(file.getAbsolutePath());
                if(jsonString.equals("[]")){
                    FileWriter fileWriter = new FileWriter(file);
                    fileWriter.write(jsonData);
                    fileWriter.close();
                }
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

    public static void initMessagesFile(Context context,String MacAdrr) {
        try {
            // Création du tableau JSON
            JSONArray jsonArray = new JSONArray();

            // Création du premier objet
            JSONObject userObject = new JSONObject();
            userObject.put("name", "SlowChien");
            userObject.put("receivedDate", getCurrentDateTime());
            userObject.put("sentDate", getCurrentDateTime());
            userObject.put("content", "Super ! Je suis sur Slowchien !");
            userObject.put("macAddressSrc", MacAdrr);
            userObject.put("macAddressDest", "AB:CD:EF:AB:CD:EF");

            // Création du deuxième objet
            JSONObject slowChienObject = new JSONObject();
            slowChienObject.put("name", "SlowChien");
            slowChienObject.put("receivedDate", getCurrentDateTime());
            slowChienObject.put("sentDate", getCurrentDateTime());
            slowChienObject.put("content", "Bienvenue dans Slowchien !");
            slowChienObject.put("macAddressSrc", "AB:CD:EF:AB:CD:EF");
            slowChienObject.put("macAddressDest", MacAdrr);

            jsonArray.put(slowChienObject);
            jsonArray.put(userObject);

            // Écriture du fichier JSON dans le stockage interne
            String jsonString = jsonArray.toString();
            JSONUtils.saveJsonFileToInternalStorage(context, MESSAGES_FILE, jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void createSentReceiveJSON(Context context, String inputFileName, String outputFileName, String filterKey) {
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

    public static void createChatJSON(Context context) {
        try {
            String jsonDirectoryPath = context.getFilesDir().getAbsolutePath() + "/" + JSON_DIRECTORY;
            String filePath = jsonDirectoryPath + "/" + MESSAGES_FILE;
            String outputFilePath = jsonDirectoryPath + "/" + CHAT_FILE;
            System.out.println(MY_MAC_ADDRESS);
            // Chargement JSON
            String content = loadJSONFromFile(filePath);
            System.out.println(content);
            // Traitement JSON
            JSONArray messages = new JSONArray(content);
            JSONArray filteredMessages = new JSONArray();

            for (int i = 0; i < messages.length(); i++) {
                JSONObject message = messages.getJSONObject(i);
                String srcAddress = message.optString("macAddressSrc", "");
                String destAddress = message.optString("macAddressDest", "");

                if (srcAddress.equals(MY_MAC_ADDRESS) || destAddress.equals(MY_MAC_ADDRESS)) {
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

    public static void initMarkersFile(Context context) {
        try {
            // Création de l'objet JSON
            JSONArray jsonArray = new JSONArray();

            JSONObject markObject = new JSONObject();
            markObject.put("latitude", 48.858324);
            markObject.put("longitude", 2.294549);
            markObject.put("titre", "Boîte au lettres n°1");
            markObject.put("desc", "PARIS - Tour Eiffel");

            jsonArray.put(markObject);

            String jsonString = jsonArray.toString();
            // Enregistrement du tableau JSON mis à jour dans le fichier
            JSONUtils.saveJsonFileToInternalStorage(context, MARKERS_FILE, jsonString);

            Log.d(TAG, "Filtrage terminé. Le fichier markers.json a été créé avec succès.");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void initContactFile(Context context,String MacAdrr) {
        try {
            // Création de l'objet JSON
            JSONArray jsonArray = new JSONArray();

            // Création du premier objet
            JSONObject userObject = new JSONObject();
            userObject.put("name", "SlowChien");
            userObject.put("macAddress", "AB:CD:EF:AB:CD:EF");
            userObject.put("address", "???");
            userObject.put("description", "Slowchien - L'appli sans réseau");

            // Création du deuxième objet
            JSONObject slowChienObject = new JSONObject();
            slowChienObject.put("name", "User");
            slowChienObject.put("macAddress", MacAdrr);
            slowChienObject.put("address", "???");
            slowChienObject.put("description", "Modifie ton profil");

            jsonArray.put(slowChienObject);
            jsonArray.put(userObject);

            // Écriture du fichier JSON dans le stockage interne
            String jsonString = jsonArray.toString();
            JSONUtils.saveJsonFileToInternalStorage(context, CONTACTS_FILE, jsonString);
        } catch (JSONException e) {
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

            Log.d(TAG, "Nettoyage terminé. Le fichier " + fileName + " a été nettoyé avec succès.");
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    public static void cleanAllJSONFiles(Context context) {
        cleanJSONFile(context, MESSAGES_FILE);
        cleanJSONFile(context, SENT_FILE);
        cleanJSONFile(context, RECEIVED_FILE);
        cleanJSONFile(context, CHAT_FILE);
        cleanJSONFile(context, MARKERS_FILE);
        initMessagesFile(context, MY_MAC_ADDRESS);
        createSentReceiveJSON(context, MESSAGES_FILE, SENT_FILE, "macAddressSrc");
        createSentReceiveJSON(context, MESSAGES_FILE, RECEIVED_FILE, "macAddressDest");
        createChatJSON(context);
        initMarkersFile(context);
    }

    public static void updateChatJSON(Context context) {
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

    public static void updateContactsJSON(Context context, String macAddress, String name, String address, String description) {
        try {
            // Récupération du fichier JSON existant depuis le stockage interne
            File directory = new File(context.getFilesDir(), JSON_DIRECTORY);
            File file = new File(directory, CONTACTS_FILE);

            // Chargement du fichier JSON
            String jsonString = loadJSONFromFile(file.getAbsolutePath());

            // Conversion la chaîne JSON en un tableau JSON
            JSONArray jsonArray = new JSONArray(jsonString);

            // Création du nouvel objet JSON
            JSONObject nouvelObjet = new JSONObject();

            nouvelObjet.put("macAddress", macAddress);
            nouvelObjet.put("name", name);
            nouvelObjet.put("address", address);
            nouvelObjet.put("description", description);

            // Ajout du nouvel objet au tableau JSON existant
            jsonArray.put(nouvelObjet);

            // Enregistrement du tableau JSON mis à jour dans le fichier
            writeJSONToFile(file.getAbsolutePath(), jsonArray.toString());

            Log.d(TAG, "Fichier JSON contact modifié avec succès.");

        } catch (JSONException e) {

            Log.e(TAG, "Erreur lors de l'ajout de la valeur JSON : " + e.getMessage());
            e.printStackTrace();
        }


    }

    public static void updateMarkersJSON(Context context, double latitude, double longitude, String titre, String desc) {
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

            Log.d(TAG, "Fichier JSON markers modifié avec succès.");

        } catch (JSONException e) {

            Log.e(TAG, "Erreur lors de l'ajout de la valeur JSON : " + e.getMessage());
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

    public static void sortMessagesByNewestDate(List<Message> messageList, String pageName) {
        Collections.sort(messageList, (message1, message2) -> {
            if (pageName.equals("Sent")) {
                return message2.getSentDate().compareTo(message1.getSentDate());
            }
            else {
                return message2.getReceivedDate().compareTo(message1.getReceivedDate());
            }
        });
    }


}
