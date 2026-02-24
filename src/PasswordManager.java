import javax.swing.SwingUtilities;

import view.PasswordManagerUI;
import controller.PasswordController;
import model.PasswordStorage;

public class PasswordManager {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PasswordStorage model = new PasswordStorage();
            PasswordController controller = new PasswordController(model);
            PasswordManagerUI ui = new PasswordManagerUI(controller);
            ui.show();
        });
    }
}