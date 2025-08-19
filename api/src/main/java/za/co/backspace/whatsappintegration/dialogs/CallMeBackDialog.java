package za.co.backspace.whatsappintegration.dialogs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import za.co.backspace.whatsappintegration.dialogs.Dialogs.DialogArgName;
import za.co.backspace.whatsappintegration.dialogs.Dialogs.DialogName;
import za.co.backspace.whatsappintegration.dialogs.Dialogs.WhatsAppDialog;
import za.co.backspace.whatsappintegration.persistence.entities.WhatsAppUser;

public class CallMeBackDialog implements WhatsAppDialog {

    @Override
    public DialogName getDialogName() {
        return DialogName.CALL_ME_BACK;
    }

    @Override
    public String initialize(WhatsAppUser whatsAppUser, Map<DialogArgName, String> dialogArgs) {
        var caseRef = "CALLBACK-0001";
        List<String> menu = new ArrayList<>();
        menu.add(String.format("Sure thing!\n\nA team member will call you back shortly, your reference is %s\n", caseRef));
        menu.add("0. Back to main menu\n");
        return String.join("\n", menu);
    }

    @Override
    public DialogResult interact(String input, WhatsAppUser whatsAppUser, Map<DialogArgName, String> dialogArgs) {
        return new DialogResult(DialogName.MAIN_MENU, Collections.emptyMap());
    }
}
