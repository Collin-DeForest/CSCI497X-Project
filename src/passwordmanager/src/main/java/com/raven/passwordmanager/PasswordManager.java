package com.raven.passwordmanager;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import com.raven.passwordmanager.view.PasswordManagerUI;
import com.raven.passwordmanager.view.PasswordManagerSignIn;
import com.raven.passwordmanager.view.PasswordManager2FA;
import com.raven.passwordmanager.controller.PasswordController;
import com.raven.passwordmanager.model.MasterPasswordManager;
import com.raven.passwordmanager.model.PasswordStorage;
import com.raven.passwordmanager.model.TwoFactor;

public class PasswordManager {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PasswordStorage model = new PasswordStorage();
            PasswordController controller = new PasswordController(model);

            TwoFactor twoFactor = new TwoFactor();

            PasswordManagerSignIn signIn = new PasswordManagerSignIn(password -> {
            PasswordManager2FA twoFactorUI = new PasswordManager2FA(twoFactor, () -> {
                try{
                    // Only derive the key after 2FA is completed
                    byte[] key = MasterPasswordManager.deriveKey(password);
                    PasswordManagerUI ui = new PasswordManagerUI(controller, key);
                    ui.show();
                }catch(Exception ex){
                    JOptionPane.showMessageDialog(null, "Error deriving key.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
            twoFactorUI.show();
        });

            signIn.show();
        });
    }
}