package com.raven.passwordmanager.controller;

import java.util.List;

import javax.swing.JOptionPane;

import com.raven.passwordmanager.model.MasterPasswordManager;
import com.raven.passwordmanager.model.PasswordEntry;
import com.raven.passwordmanager.model.PasswordStorage;
import com.raven.passwordmanager.model.TwoFactor;
import com.raven.passwordmanager.view.PasswordManager2FA;
import com.raven.passwordmanager.view.PasswordManagerSignIn;
import com.raven.passwordmanager.view.PasswordManagerUI;

public class PasswordController{
private final PasswordStorage model;
private final TwoFactor twoFA;

    public PasswordController(PasswordStorage model){
        this.model = model;
        this.twoFA = new TwoFactor();
    }

    public void addEntry(String site, String username, String password){
        if(site.isBlank() || username.isBlank() || password.isBlank()){
            throw new IllegalArgumentException("All fields must be filled out.");
        }
        model.addEntry(new PasswordEntry(site, username, password));
    }

    public void start() {
        PasswordManagerSignIn signIn = new PasswordManagerSignIn(password -> {
            handleSignIn(password);
        });

        signIn.show();
    }

    private void handleSignIn(String password) {
        boolean setupRequired = twoFA.isSetupRequired();

        PasswordManager2FA twoFactorUI =
            new PasswordManager2FA(twoFA, setupRequired, () -> {
                openMainUI(password);
            });

        twoFactorUI.show();
    }

    private void openMainUI(String password) {
        try {
            byte[] key = MasterPasswordManager.deriveKey(password);
            PasswordManagerUI ui = new PasswordManagerUI(this, key);
            ui.show();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error deriving key.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void deleteEntry(int index){
        model.deleteEntry(index);
    }

    public void updateEntry(int index, String newPassword){
        model.updateEntry(index, newPassword);
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