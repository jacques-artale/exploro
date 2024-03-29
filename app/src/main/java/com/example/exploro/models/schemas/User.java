package com.example.exploro.models.schemas;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class User {
    public String id, name, username, password, email, phoneNumber;
    public int experience;

    public User() {}

    public User(String name, String email, String username, String password, String phoneNumber){
        this.name = name;
        this.email = email;
        this.username = username;
        this.password = password;
        this.phoneNumber = phoneNumber;
    }
    public User(String name, String password){
        this.name = name;
        this.password = password;
        this.email = email;
        this.phoneNumber = phoneNumber;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() { return username; }

    public void setUsername (String username) { this.username = username; }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public int getExperience() {
        return experience;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }
}
