package view;

import javax.swing.*;
import java.awt.*;

public class PasswordManagerSignIn {

    private final JFrame frame;

    public PasswordManagerSignIn(Runnable onContinue) {

        frame = new JFrame("Sign In");
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel label = new JLabel("Welcome to Password Manager");
        label.setHorizontalAlignment(SwingConstants.CENTER);

        JButton okButton = new JButton("OK");

        okButton.addActionListener(e -> {
            frame.dispose();     
            onContinue.run();
        });

        panel.add(label, BorderLayout.CENTER);
        panel.add(okButton, BorderLayout.SOUTH);

        frame.setContentPane(panel);
        frame.pack();
    }

    public void show() {
        frame.setVisible(true);
    }
}