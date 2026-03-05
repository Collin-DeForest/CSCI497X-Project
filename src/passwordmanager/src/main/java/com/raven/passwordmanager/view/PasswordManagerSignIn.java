package com.raven.passwordmanager.view;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.function.Consumer;
import com.raven.passwordmanager.model.MasterPasswordManager;

// Sign in screen handles both a first time user and a returning user
public class PasswordManagerSignIn {

    private final JFrame frame;

    public PasswordManagerSignIn(Consumer<String> onContinue){

        frame = new JFrame("Sign In");
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Check if master password has been set up before
        if(new File("master.txt").exists()){
            showLoginScreen(onContinue);
        }else{
            showFirstTimeSetup(onContinue);
        }

        frame.pack();
    }

    // Returning user just needs to enter their master password to proceed
    private void showLoginScreen(Consumer<String> onContinue){
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        JLabel title = new JLabel("Welcome Back!");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        title.setHorizontalAlignment(SwingConstants.CENTER);

        // Password input
        JPanel inputPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        JLabel passwordLabel = new JLabel("Master Password:");
        JPasswordField passwordField = new JPasswordField();
        inputPanel.add(passwordLabel);
        inputPanel.add(passwordField);

        // Unlock button
        JButton unlockButton = new JButton("Unlock");
        unlockButton.addActionListener(e -> {
            String enteredPassword = new String(passwordField.getPassword());
            if(enteredPassword.isBlank()){
                JOptionPane.showMessageDialog(frame, "Please enter your master password.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // Verify if the user entered a matching master password
            try{
                if(!MasterPasswordManager.verifyMasterPassword(enteredPassword)){
                    JOptionPane.showMessageDialog(frame, "Incorrect password.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                // Pass the master password forward the key will be derived after 2FA
                frame.dispose();
                onContinue.accept(enteredPassword);
            }catch(Exception ex){
                JOptionPane.showMessageDialog(frame, "Error verifying password.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        });

        panel.add(title, BorderLayout.NORTH);
        panel.add(inputPanel, BorderLayout.CENTER);
        panel.add(unlockButton, BorderLayout.SOUTH);

        frame.setContentPane(panel);
    }

    // first time setup create a new master password and store it
    private void showFirstTimeSetup(Consumer<String> onContinue){
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        JLabel title = new JLabel("Create Master Password");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        title.setHorizontalAlignment(SwingConstants.CENTER);

        // Password inputs
        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        JLabel newPasswordLabel = new JLabel("New Password:");
        JPasswordField newPasswordField = new JPasswordField();
        JLabel confirmPasswordLabel = new JLabel("Confirm Password:");
        JPasswordField confirmPasswordField = new JPasswordField();
        inputPanel.add(newPasswordLabel);
        inputPanel.add(newPasswordField);
        inputPanel.add(confirmPasswordLabel);
        inputPanel.add(confirmPasswordField);

        // Create button
        JButton createButton = new JButton("Create");
        createButton.addActionListener(e -> {
            String newPassword = new String(newPasswordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());

            // Validation fields for master password(Needs to be atleast 8 characters long, have a capital letter and a special character to be valid)
            if(newPassword.isBlank()){
                JOptionPane.showMessageDialog(frame, "Please enter a password.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if(!newPassword.equals(confirmPassword)){
                JOptionPane.showMessageDialog(frame, "Passwords do not match.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if(newPassword.length() < 8){
                JOptionPane.showMessageDialog(frame, "Password must be at least 8 characters.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if(!newPassword.matches(".*[A-Z].*")){
                JOptionPane.showMessageDialog(frame, "Password must contain at least one uppercase letter.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if(!newPassword.matches(".*[!@#$%^&*()_+\\-=\\[\\]{}|;':\",./<>?].*")){
                JOptionPane.showMessageDialog(frame, "Password must contain at least one special character.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // create the master password and save it
            try{
                MasterPasswordManager.createMasterPassword(newPassword);
                // Pass the master password forward the key will be derived after 2FA
                frame.dispose();
                onContinue.accept(newPassword);
            }catch(Exception ex){
                JOptionPane.showMessageDialog(frame, "Error saving password.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        });

        panel.add(title, BorderLayout.NORTH);
        panel.add(inputPanel, BorderLayout.CENTER);
        panel.add(createButton, BorderLayout.SOUTH);

        frame.setContentPane(panel);
    }

    public void show(){
        frame.setVisible(true);
    }
}