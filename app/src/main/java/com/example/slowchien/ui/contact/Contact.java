package com.example.slowchien.ui.contact;

public class Contact {

    private final String name;
    private final String address;
    private final String description;
    private final String macAddress;

    public Contact(String name, String address, String description, String macAddress) {
        this.name = name;
        this.address = address;
        this.description = description;
        this.macAddress = macAddress;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getDescription() {
        return description;
    }

    public String getMacAddress() {
        return macAddress;
    }

    @Override
    public String toString() {
        return "Contact{" +
                "name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", description='" + description + '\'' +
                ", macAddress='" + macAddress + '\'' +
                '}';
    }
}
