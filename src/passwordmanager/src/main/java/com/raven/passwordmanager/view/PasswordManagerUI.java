package com.raven.passwordmanager.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import com.raven.passwordmanager.controller.PasswordController;

import java.awt.*;

public class PasswordManagerUI {

    private final JFrame frame;
    private final JTextField siteField;
    private final JTextField userField;
    private final JPasswordField passField;
    private final PasswordController controller;
    private final DefaultTableModel tableModel;
    private JTable table;

    public PasswordManagerUI(PasswordController controller) {
        this.controller = controller;

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
        header.add(title);
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
        JButton generatePassBtn = new JButton("Generate");
        generatePassBtn.addActionListener(e -> passField.setText(controller.generatePassword()));

        JButton toggleVisBtn = new JButton("Show");
        toggleVisBtn.addActionListener(e -> {
            if (passField.getEchoChar() == 0) {
                passField.setEchoChar('•');
                toggleVisBtn.setText("Show");
            } else {
                passField.setEchoChar((char) 0);
                toggleVisBtn.setText("Hide");
            }
        });

        addRow(card, c, 0, "Website / App", siteField);
        addRow(card, c, 1, "Username", userField);
        addRow(card, c, 2, "Password", passField);

        // Add generate button on same row
        c.gridx = 2;
        c.gridy = 2;
        c.weightx = 0;
        card.add(generatePassBtn, c);

        c.gridx = 3;
        card.add(toggleVisBtn, c);

        // Table to display saved entries
        String[] columns = {"Website / App", "Username", "Password"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(24);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Saved Passwords"));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, card, scrollPane);
        splitPane.setDividerLocation(200);
        root.add(splitPane, BorderLayout.CENTER);

        // Button bar
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton addBtn = new JButton("Add");
        JButton retrieveBtn = new JButton("Retrieve");
        JButton deleteBtn = new JButton("Delete");
        JButton clearBtn = new JButton("Clear");
        JButton checkBtn = new JButton("Check Password");
        JButton checkAllBtn = new JButton("Check All Passwords");
        JButton exitBtn = new JButton("Exit");

        // Add
        addBtn.addActionListener(e -> {
            try {
                controller.addEntry(siteField.getText(), userField.getText(), new String(passField.getPassword()));
                refreshTable();
                clearFields();
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Delete
        deleteBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(frame, "Please select an entry to delete.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            controller.deleteEntry(selectedRow);
            refreshTable();
        });

        // Retrieve
        retrieveBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(frame, "Please select an entry to retrieve.", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(frame,
                    "Website/App: " + controller.getEntry(selectedRow).getSite() +
                    "\nUsername: "  + controller.getEntry(selectedRow).getUsername() +
                    "\nPassword: "  + controller.getEntry(selectedRow).getPassword(),
                    "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        // Clear
        clearBtn.addActionListener(e -> clearFields());

        // Check selected password
        checkBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(frame, "Please select an entry to check.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            checkBtn.setEnabled(false);
            checkBtn.setText("Checking…");
            int row = selectedRow;
            new SwingWorker<String, Void>() {
                @Override protected String doInBackground() {
                    return controller.auditEntry(row);
                }
                @Override protected void done() {
                    checkBtn.setEnabled(true);
                    checkBtn.setText("Check Password");
                    try {
                        String result = get();
                        if (result == null) {
                            JOptionPane.showMessageDialog(frame, "✓  Password is safe.", "Check Password", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(frame, result, "⚠  Password at Risk", JOptionPane.WARNING_MESSAGE);
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }.execute();
        });

        // Check all passwords
        checkAllBtn.addActionListener(e -> {
            if (controller.getEntryCount() == 0) {
                JOptionPane.showMessageDialog(frame, "No entries to check.", "Check All Passwords", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            checkAllBtn.setEnabled(false);
            checkAllBtn.setText("Checking…");
            new SwingWorker<String, Void>() {
                @Override protected String doInBackground() {
                    return controller.auditAllEntries();
                }
                @Override protected void done() {
                    checkAllBtn.setEnabled(true);
                    checkAllBtn.setText("Check All Passwords");
                    try {
                        String report = get();
                        if (report == null) {
                            JOptionPane.showMessageDialog(frame, "✓  All passwords are safe.", "Check All Passwords", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JTextArea textArea = new JTextArea(report);
                            textArea.setEditable(false);
                            textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
                            JScrollPane sp = new JScrollPane(textArea);
                            sp.setPreferredSize(new Dimension(500, 200));
                            JOptionPane.showMessageDialog(frame, sp, "⚠  Passwords at Risk", JOptionPane.WARNING_MESSAGE);
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }.execute();
        });

        // Exit
        exitBtn.addActionListener(e -> frame.dispose());

        buttons.add(addBtn);
        buttons.add(retrieveBtn);
        buttons.add(clearBtn);
        buttons.add(deleteBtn);
        buttons.add(checkBtn);
        buttons.add(checkAllBtn);
        buttons.add(exitBtn);

        root.add(buttons, BorderLayout.SOUTH);
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (int i = 0; i < controller.getEntryCount(); i++) {
            tableModel.addRow(new Object[]{
                controller.getEntry(i).getSite(),
                controller.getEntry(i).getUsername(),
                "********"
            });
        }
    }

    public void show() {
        frame.setVisible(true);
    }

    private void clearFields() {
        siteField.setText("");
        userField.setText("");
        passField.setText("");
        siteField.requestFocusInWindow();
    }

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