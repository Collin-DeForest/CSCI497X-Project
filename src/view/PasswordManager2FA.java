package view;
import javax.swing.*;
import java.awt.*;

public class PasswordManager2FA {
    private final JFrame frame;
    private final JTextField codeField;

    // Demo
    private static final String code = "123456";

    public PasswordManager2FA(Runnable onSuccess) {

        frame = new JFrame("Two-Factor Authentication");
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel label = new JLabel("Enter 6-Digit Code: ");
        codeField = new JTextField();

        JButton verifyButton = new JButton("Verify");

        verifyButton.addActionListener(e -> {
            String entered = codeField.getText();

            if (code.equals(entered)) {
                frame.dispose();
                onSuccess.run();
            } else {
                JOptionPane.showMessageDialog(frame, "Invalid code", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(label, BorderLayout.NORTH);
        panel.add(codeField, BorderLayout.CENTER);
        panel.add(verifyButton, BorderLayout.SOUTH);

        frame.setContentPane(panel);
        frame.pack();
    }

    public void show() {
        frame.setVisible(true);
    }
}
