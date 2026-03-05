package com.raven.passwordmanager.controller;

import java.util.List;

import com.raven.passwordmanager.model.PasswordEntry;
import com.raven.passwordmanager.model.PasswordStorage;

public class PasswordController{
    private final PasswordStorage model;

    public PasswordController(PasswordStorage model){
        this.model = model;
    }

    public void addEntry(String site, String username, String password){
        if(site.isBlank() || username.isBlank() || password.isBlank()){
            throw new IllegalArgumentException("All fields must be filled out.");
        }
        model.addEntry(new PasswordEntry(site, username, password));
    }

    public void deleteEntry(int index){
        model.deleteEntry(index);
    }

    public PasswordEntry getEntry(int index){
        return model.getEntry(index);
    }

    public int getEntryCount(){
        return model.size();
    }

    public List<PasswordEntry> getAllEntries(){
        return model.getAllEntries();
    }

}