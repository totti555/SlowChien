package com.example.slowchien.ui.home;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Message implements Parcelable {
    private final String title;
    private final Date receivedDate;
    private final Date sentDate;
    private final String name;
    private final String macAddress;

    public Message(String title, Date receivedDate, Date sentDate, String name, String macAddress) {
        this.title = title;
        this.receivedDate = receivedDate;
        this.sentDate = sentDate;
        this.name = name;
        this.macAddress = macAddress;
    }

    protected Message(Parcel in) {
        title = in.readString();
        name = in.readString();
        macAddress = in.readString();
        try {
            receivedDate = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).parse(in.readString());
            sentDate = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).parse(in.readString());
        } catch (ParseException e) {
            throw new RuntimeException("Failed to parse date", e);
        }
    }

    public static final Creator<Message> CREATOR = new Creator<Message>() {
        @Override
        public Message createFromParcel(Parcel in) {
            return new Message(in);
        }

        @Override
        public Message[] newArray(int size) {
            return new Message[size];
        }
    };

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

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(name);
        dest.writeString(macAddress);
        dest.writeString(new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(receivedDate));
        dest.writeString(new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(sentDate));
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
