package com.example.slowchien.ui.home;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Message {
    private final String title;
    private final Date receivedDate;
    private final  Date sentDate;
    private final String name;
    private final String macAddress;

    public Message(String title, Date receivedDate, Date sentDate, String name, String macAddress ) {
        this.name = name;
        this.macAddress = macAddress;
        this.title = title;
        this.receivedDate = receivedDate;
        this.sentDate = sentDate;
    }

    public String getTitle() {
        return title;
    }

    public String getFormattedReceivedDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return dateFormat.format(receivedDate);
    }

    public String getFormattedSentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return dateFormat.format(sentDate);
    }

    public String getMacAddress() {
        return macAddress;
    }

    public String getName() {
        return name;
    }
}
