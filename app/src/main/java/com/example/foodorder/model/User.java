package com.example.foodorder.model;

public class User {
    private String Name;
    private String Password;
    private String Phone;
    private String uid;

    public User() {
    }

    public User(String name, String password) {
        Name = name;
        Password = password;
    }

    public User(String name, String password, String uid) {
        Name = name;
        Password = password;
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }
}
