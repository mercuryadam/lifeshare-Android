package com.lifeshare.model;

public class ContactListModel {
    public boolean isSelected = false;
    private String name;
    private String email;
    private String mobile;
    private String profile;

    public Boolean getSelected() {
        return isSelected;
    }

    public void setSelected(Boolean selected) {
        isSelected = selected;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        mobile = mobile.replaceAll("[^\\d]", "");
        mobile = mobile.trim();
        mobile = mobile.replaceAll(" +", "");
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }
}
