import javax.swing.SwingUtilities;

import view.PasswordManagerUI;
import view.PasswordManagerSignIn;
import view.PasswordManager2FA;
import controller.PasswordController;
import model.PasswordStorage;

public class PasswordManager {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PasswordStorage model = new PasswordStorage();
            PasswordController controller = new PasswordController(model);
            PasswordManagerUI ui = new PasswordManagerUI(controller);

            PasswordManager2FA twoFactor = new PasswordManager2FA(ui::show);

            PasswordManagerSignIn signIn = new PasswordManagerSignIn(twoFactor::show);

            signIn.show();
        });
    }
}