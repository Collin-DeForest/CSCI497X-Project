package com.raven.passwordmanager.model;

import java.util.ArrayList;
import java.util.List;

public class PasswordStorage{

    private final List<PasswordEntry> entries = new ArrayList<>();

    public void addEntry(PasswordEntry entry){
        entries.add(entry);
    }

    public void deleteEntry(int index){
        entries.remove(index);
    }
    
    public void updateEntry(int index, String newPassword){
        entries.get(index).setPassword(newPassword);
    }

    public PasswordEntry getEntry(int index){
        return entries.get(index);
    }

    public List<PasswordEntry> getAllEntries(){
        return entries;
    }

    public int size(){
        return entries.size();
    }
}