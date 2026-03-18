package com.raven.passwordmanager.controller;

import java.util.List;

import javax.swing.JOptionPane;

import com.raven.passwordmanager.model.TwoFactor;
import com.raven.passwordmanager.view.PasswordManager2FA;
import com.raven.passwordmanager.view.PasswordManagerSignIn;
import com.raven.passwordmanager.view.PasswordManagerUI;
import com.raven.passwordmanager.model.HaveIBeenPwnedChecker;
import com.raven.passwordmanager.model.HaveIBeenPwnedChecker.PwnedResult;
import com.raven.passwordmanager.model.MasterPasswordManager;
import com.raven.passwordmanager.model.PasswordEntry;
import com.raven.passwordmanager.model.PasswordGenerator;
import com.raven.passwordmanager.model.PasswordGenerator.GeneratorOptions;
import com.raven.passwordmanager.model.PasswordStorage;
import com.raven.passwordmanager.model.PasswordStrengthChecker;
import com.raven.passwordmanager.model.PasswordStrengthChecker.StrengthResult;

public class PasswordController{
private final PasswordStorage model;
private final TwoFactor twoFA;
private final PasswordStrengthChecker strengthChecker = new PasswordStrengthChecker();
private final PasswordGenerator generator = new PasswordGenerator();
private final HaveIBeenPwnedChecker pwnedChecker = new HaveIBeenPwnedChecker();

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

public String auditEntry(int index) {
        PasswordEntry entry = model.getEntry(index);
        return buildRiskLine(entry.getSite(), entry.getPassword());
    }

    // Check all entries, returns null if everything is safe
    public String auditAllEntries() {
        StringBuilder report = new StringBuilder();
        for (int i = 0; i < model.size(); i++) {
            PasswordEntry entry = model.getEntry(i);
            String line = buildRiskLine(entry.getSite(), entry.getPassword());
            if (line != null) report.append(line).append("\n");
        }
        return report.length() == 0 ? null : report.toString().trim();
    }

    // Returns a risk summary for one password, or null if it's safe
    private String buildRiskLine(String site, String password) {
        StrengthResult strength = strengthChecker.evaluate(password);
        PwnedResult pwned = pwnedChecker.check(password);

        boolean weak = strength.getScore() < 60;
        boolean compromised = !pwned.isError() && pwned.isPwned();

        if (!weak && !compromised) return null;

        StringBuilder line = new StringBuilder(site).append(" — ");

        if (weak)
            line.append(strength.getLabel()).append(" (").append(strength.getScore()).append("/100)");
        if (weak && compromised)
            line.append(", ");
        if (compromised)
            line.append("pwned ").append(pwned.getCount()).append(" time(s)");
        if (pwned.isError())
            line.append(weak ? " [breach check unavailable]" : "[breach check unavailable]");

        return line.toString();
    }

    public StrengthResult checkStrength(String password)     { return strengthChecker.evaluate(password); }
    public String generatePassword()                         { return generator.generate(); }
    public String generatePassword(GeneratorOptions options) { return generator.generate(options); }
    public PwnedResult checkPwned(String password)           { return pwnedChecker.check(password); }
}
