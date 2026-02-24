import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class PasswordManagerUI {

    private final JFrame frame;
    private final JTextField siteField;
    private final JTextField userField;
    private final JPasswordField passField;

    public PasswordManagerUI() {

        // main frame setup
        frame = new JFrame("Local Password Manager");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 380);
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

        // Button bar (clear, add, exit)
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton addBtn = new JButton("Add");
        JButton clearBtn = new JButton("Clear");
        JButton exitBtn = new JButton("Exit");

        // clear button listener 
        clearBtn.addActionListener(e -> clearFields());

        // exit button listener
        exitBtn.addActionListener(e -> frame.dispose());

        buttons.add(clearBtn);
        buttons.add(addBtn);
        buttons.add(exitBtn);

        root.add(buttons, BorderLayout.SOUTH);
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