package com.raven.passwordmanager.view;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.raven.passwordmanager.model.TwoFactor;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class PasswordManager2FA {
    private final JFrame frame;
    private final JTextField codeField;

    public PasswordManager2FA(TwoFactor twoFactor,boolean showQRCode, Runnable onSuccess) {
        frame = new JFrame("Two-Factor Authentication");
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // main panel
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // QR code panel
        if(showQRCode == true){
            String otpAuthURL = twoFactor.getOtpAuthURL("user@example.com", "PasswordManager");
            BufferedImage QRCodeImage = generateQRCodeImage(otpAuthURL);
            JLabel QRCodeLabel = new JLabel(new ImageIcon(QRCodeImage));
            JPanel QRCodePanel = new JPanel(new FlowLayout());
            QRCodePanel.add(QRCodeLabel);
            panel.add(QRCodePanel);
        }
        

        JLabel label = new JLabel("Enter 6-Digit Code: ");
        codeField = new JTextField(6);
        JPanel codePanel = new JPanel(new FlowLayout());
        codePanel.add(label);
        codePanel.add(codeField);
        panel.add(codePanel);

        JButton verifyButton = new JButton("Verify");

        verifyButton.addActionListener(e -> {
            String entered = codeField.getText();
            try {
                if (twoFactor.verify(entered)) {
                    frame.dispose();
                    onSuccess.run();
                } else {
                    JOptionPane.showMessageDialog(frame, "Invalid code", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Verification failed", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(verifyButton);
        panel.add(buttonPanel);

        frame.setContentPane(panel);
        frame.pack();
    }

    public void show() {
        frame.setVisible(true);
    }

    // QR code generation method
    private BufferedImage generateQRCodeImage(String otpAuthURL) {
        try {
            MultiFormatWriter barcodeWriter = new MultiFormatWriter();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.MARGIN, 1);
            BitMatrix bitMatrix = barcodeWriter.encode(otpAuthURL, BarcodeFormat.QR_CODE, 200, 200, hints);

            BufferedImage QRCodeImage = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
            for (int x = 0; x < 200; x++) {
                for (int y = 0; y < 200; y++) {
                    QRCodeImage.setRGB(x, y, bitMatrix.get(x, y) ? 0x000000 : 0xFFFFFF);
                }
            }
            return QRCodeImage;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }
}