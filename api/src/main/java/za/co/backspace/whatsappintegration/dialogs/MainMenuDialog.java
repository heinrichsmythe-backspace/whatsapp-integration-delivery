package za.co.backspace.whatsappintegration.dialogs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import za.co.backspace.whatsappintegration.dialogs.Dialogs.DialogArgName;
import za.co.backspace.whatsappintegration.dialogs.Dialogs.DialogName;
import za.co.backspace.whatsappintegration.dialogs.Dialogs.WhatsAppDialog;
import za.co.backspace.whatsappintegration.persistence.entities.WhatsAppUser;

public class MainMenuDialog implements WhatsAppDialog {
    @Override
    public DialogName getDialogName() {
        return DialogName.MAIN_MENU;
    }

    @Override
    public String initialize(WhatsAppUser whatsAppUser, Map<DialogArgName, String> dialogArgs) {
        List<String> menu = new ArrayList<>();
        menu.add(String.format("👋👋 Hello %s\n", whatsAppUser.getFirstName()));

        menu.add("Welcome to Backspace.\n");
        menu.add("How can we help you today?\n\n");
        menu.add("1. Speak to Support");
        menu.add("2. Speak to Sales");
        menu.add("3. Call me back");
        return String.join("\n", menu);
    }

    @Override
    public DialogResult interact(String input, WhatsAppUser whatsAppUser, Map<DialogArgName, String> dialogArgs) {
        switch (input) {
            case "1":
                return new DialogResult(DialogName.SUPPORT_CONVERSATION, Collections.emptyMap());
            case "2":
                return new DialogResult(DialogName.SALES_CONVERSATION, Collections.emptyMap());
            case "3":
                return new DialogResult(DialogName.CALL_ME_BACK, Collections.emptyMap());
            default:
                return new DialogResult(DialogName.MAIN_MENU, Collections.emptyMap());
        }
    }
}
