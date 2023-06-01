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
    private final String macAddressSrc;
    private final String macAddressDest;


    public Message(String title, Date receivedDate, Date sentDate, String name, String macAddressSrc, String macAddressDest) {
        this.title = title;
        this.receivedDate = receivedDate;
        this.sentDate = sentDate;
        this.name = name;
        this.macAddressSrc = macAddressSrc;
        this.macAddressDest = macAddressDest;
    }

    public Message(Date receivedDate, Date sentDate, String name, String macAddressSrc, String macAddressDest) {
        this.title = "";
        this.receivedDate = receivedDate;
        this.sentDate = sentDate;
        this.name = name;
        this.macAddressSrc = macAddressSrc;
        this.macAddressDest = macAddressDest;
    }

    protected Message(Parcel in) {
        title = in.readString();
        name = in.readString();
        macAddressSrc = in.readString();
        macAddressDest = in.readString();
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

    @Override
    public String toString() {
        return "Message{" +
                "sentDate=" + sentDate +
                ", receiveDate=" + receivedDate +
                ", content='" + name + '\'' +
                '}';
    }

    public String getTitle() {
        return title;
    }

    public Date getReceivedDate() {
        return receivedDate;
    }

    public Date getSentDate() {
        return sentDate;
    }



    public String getFormattedReceivedDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return dateFormat.format(receivedDate);
    }

    public String getFormattedSentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return dateFormat.format(sentDate);
    }

    public String getMacAddressSrc() {
        return macAddressSrc;
    }

    public String getMacAddressDest() {
        return macAddressDest;
    }

    public String getName() {
        return name;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(name);
        dest.writeString(macAddressSrc);
        dest.writeString(macAddressDest);
        dest.writeString(new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(receivedDate));
        dest.writeString(new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(sentDate));
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
