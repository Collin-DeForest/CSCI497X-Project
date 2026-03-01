package com.raven.passwordmanager;
import javax.swing.SwingUtilities;
import com.raven.passwordmanager.view.PasswordManagerUI;
import com.raven.passwordmanager.view.PasswordManagerSignIn;
import com.raven.passwordmanager.view.PasswordManager2FA;
import com.raven.passwordmanager.controller.PasswordController;
import com.raven.passwordmanager.model.PasswordStorage;
import com.raven.passwordmanager.model.TwoFactor;

public class PasswordManager {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PasswordStorage model = new PasswordStorage();
            PasswordController controller = new PasswordController(model);
            PasswordManagerUI ui = new PasswordManagerUI(controller);

            TwoFactor twoFactor = new TwoFactor();

            PasswordManager2FA twoFactorUI = new PasswordManager2FA(twoFactor, ui::show);

            PasswordManagerSignIn signIn = new PasswordManagerSignIn(twoFactorUI::show);

            signIn.show();
        });
    }
}