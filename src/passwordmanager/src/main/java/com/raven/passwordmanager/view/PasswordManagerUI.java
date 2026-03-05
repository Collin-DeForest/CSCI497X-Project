package com.raven.passwordmanager.view;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import com.raven.passwordmanager.controller.PasswordController;
import com.raven.passwordmanager.model.PasswordEntry;
import com.raven.passwordmanager.model.VaultEncryption;
import java.util.List;

import java.awt.*;

public class PasswordManagerUI {

    private final JFrame frame;
    private final JTextField siteField;
    private final JTextField userField;
    private final JPasswordField passField;
    private final PasswordController controller;
    private final DefaultTableModel tableModel;
    private final byte[] key;

    public PasswordManagerUI(PasswordController controller, byte[] key){
        this.controller = controller;
        this.key = key;

        // main frame setup
        frame = new JFrame("Local Password Manager");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 550);
        frame.setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(16, 16));
        root.setBorder(new EmptyBorder(16, 16, 16, 16));
        frame.setContentPane(root);

        // Header
        JPanel header = new JPanel(new GridLayout(2, 1));
        JLabel title = new JLabel("Password Vault");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        JLabel subtitle = new JLabel("Phase 1: UI skeleton (not functional yet)");
        subtitle.setFont(subtitle.getFont().deriveFont(13f));
        header.add(title);
        header.add(subtitle);
        root.add(header, BorderLayout.NORTH);

        // Card panel for inputs
        JPanel card = new JPanel(new GridBagLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 210, 210)),
                new EmptyBorder(14, 14, 14, 14)
        ));
        root.add(card, BorderLayout.CENTER);

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;

        siteField = new JTextField();
        userField = new JTextField();
        passField = new JPasswordField();

        addRow(card, c, 0, "Website / App", siteField);
        addRow(card, c, 1, "Username", userField);
        addRow(card, c, 2, "Password", passField);

        // Table to display saved entries
        String[] columns = {"Website / App", "Username", "Password"};
        tableModel = new DefaultTableModel(columns, 0) {
            // make sure you cant edit the table
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Decrypt the vault file and load all the saved entries on startup
        try{
            List<PasswordEntry> entries = VaultEncryption.decrypt(key);
            for(PasswordEntry entry : entries){
                controller.addEntry(entry.getSite(), entry.getUsername(), entry.getPassword());
            }
        }catch(Exception ex){
            JOptionPane.showMessageDialog(null, "Error loading vault.", "Error", JOptionPane.ERROR_MESSAGE);
        }

        JTable table = new JTable(tableModel);
        table.setRowHeight(24);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Saved Passwords"));
        
        // Split input card and table vertically
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, card, scrollPane);
        splitPane.setDividerLocation(200);
        root.add(splitPane, BorderLayout.CENTER);

        // Button bar (clear, add, retrieve, delete, exit)
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton addBtn = new JButton("Add");
        JButton retrieveBtn = new JButton("Retrieve");
        JButton deleteBtn = new JButton("Delete");
        JButton clearBtn = new JButton("Clear");
        JButton exitBtn = new JButton("Exit");
        
        // Add button listener
        addBtn.addActionListener(e -> {
            try{
                controller.addEntry(siteField.getText(), userField.getText(), new String(passField.getPassword()));
                refreshTable();
                clearFields();
            }catch(IllegalArgumentException ex){
                JOptionPane.showMessageDialog(frame, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Delete button listener
        deleteBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if(selectedRow == -1){
                JOptionPane.showMessageDialog(frame, "Please select an entry to delete.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            controller.deleteEntry(selectedRow);
            refreshTable();
        });

        // Show the credentials for the selected entry in a popup
        retrieveBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if(selectedRow == -1){
                JOptionPane.showMessageDialog(frame, "Please select an entry to retrieve.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String site = controller.getEntry(selectedRow).getSite();
            String username = controller.getEntry(selectedRow).getUsername();
            String password = controller.getEntry(selectedRow).getPassword();
            JOptionPane.showMessageDialog(frame,
                "Website / App: " + site + "\nUsername: " + username + "\nPassword: " + password,
                "Login Information", JOptionPane.INFORMATION_MESSAGE);
        });

        // clear button listener 
        clearBtn.addActionListener(e -> clearFields());

        // exit button listener, encrypts and saves all entires to vault.txt then closes the app
        exitBtn.addActionListener(e -> {
            try{
                VaultEncryption.encrypt(controller.getAllEntries(), key);
            }catch(Exception ex){
                JOptionPane.showMessageDialog(frame, "Error saving vault.", "Error", JOptionPane.ERROR_MESSAGE);
            }
            frame.dispose();
        });

        buttons.add(addBtn);
        buttons.add(retrieveBtn);
        buttons.add(clearBtn);
        buttons.add(deleteBtn);
        buttons.add(exitBtn);

        root.add(buttons, BorderLayout.SOUTH);

        refreshTable();
    }

    // Refreshes the table with current entries from the controller
    private void refreshTable(){
        tableModel.setRowCount(0);
        for(int i = 0; i < controller.getEntryCount(); i++){
            tableModel.addRow(new Object[]{
                controller.getEntry(i).getSite(),
                controller.getEntry(i).getUsername(),
                "********"
            });
        }
    }

    // called by PasswordManager main method
    public void show() {
        frame.setVisible(true);
    }

    // helper method to clear input fields, not useful yet but will be later
    private void clearFields() {
        siteField.setText("");
        userField.setText("");
        passField.setText("");
        siteField.requestFocusInWindow();
    }
    // helper method to add label and field to card panel
    private static void addRow(JPanel panel, GridBagConstraints c, int row, String labelText, JComponent field) {
        c.gridx = 0;
        c.gridy = row;
        c.weightx = 0;
        panel.add(new JLabel(labelText), c);

        c.gridx = 1;
        c.weightx = 1;
        panel.add(field, c);
    }
}