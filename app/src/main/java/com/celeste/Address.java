package com.celeste;

public class Address {
   private String address;
   private String city ;
   private String state ;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getKnonName() {
        return knonName;
    }

    public void setKnonName(String knonName) {
        this.knonName = knonName;
    }

    private String country ;
   private String postalCode;
   private String knonName ;

    public Address(String address, String city, String state, String country, String postalCode, String knonName) {
        this.address = address;
        this.city = city;
        this.state = state;
        this.country = country;
        this.postalCode = postalCode;
        this.knonName = knonName;
    }
}

