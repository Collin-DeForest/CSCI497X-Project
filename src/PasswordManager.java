import javax.swing.SwingUtilities;

public class PasswordManager {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PasswordManagerUI ui = new PasswordManagerUI();
            ui.show();
        });
    }
}