package com.sujay.apps.sarvamart;

import java.io.Serializable;

public class Address {
    String name;
    String mobileNumber;
    String addressLine1;
    String addressLine2;
    String landmarkText;
    String pincodeText;
    String townCity;
    String state;
    boolean defaultAdd;

    public Address() {
    }

    public Address(String name, String mobileNumber, String addressLine1, String addressLine2, String landmarkText, String pincodeText, String townCity, String state, boolean defaultAdd) {
        this.name = name;
        this.mobileNumber = mobileNumber;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.landmarkText = landmarkText;
        this.pincodeText = pincodeText;
        this.townCity = townCity;
        this.state = state;
        this.defaultAdd = defaultAdd;
    }

    public Address(String name, String mobileNumber, String addressLine1, String addressLine2, String landmarkText, String pincodeText, String townCity, String state) {
        this.name = name;
        this.mobileNumber = mobileNumber;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.landmarkText = landmarkText;
        this.pincodeText = pincodeText;
        this.townCity = townCity;
        this.state = state;
    }

    public String getName() {
        return name;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public String getLandmarkText() {
        return landmarkText;
    }

    public String getPincodeText() {
        return pincodeText;
    }

    public String getTownCity() {
        return townCity;
    }

    public String getState() {
        return state;
    }

    public boolean isDefaultAdd() {
        return defaultAdd;
    }
}
