package models;

public class UserInfo {
    private String name;
    private String emailAddress;
    private String mobileNumber;
    private String address;

    public UserInfo() {

    }

    public UserInfo(String name, String emailAddress, String mobileNumber) {
        this.name = name;
        this.emailAddress = emailAddress;
        this.mobileNumber = mobileNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
