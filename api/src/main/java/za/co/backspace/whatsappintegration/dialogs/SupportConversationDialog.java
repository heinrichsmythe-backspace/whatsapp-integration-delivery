package za.co.backspace.whatsappintegration.dialogs;

import java.util.HashMap;
import java.util.Map;

import za.co.backspace.whatsappintegration.dialogs.Dialogs.DialogArgName;
import za.co.backspace.whatsappintegration.dialogs.Dialogs.DialogName;
import za.co.backspace.whatsappintegration.dialogs.Dialogs.WhatsAppDialog;
import za.co.backspace.whatsappintegration.persistence.entities.WhatsAppUser;

public class SupportConversationDialog implements WhatsAppDialog {

    private static Map<String, String> testCasesDb = new HashMap<>();

    @Override
    public DialogName getDialogName() {
        return DialogName.SUPPORT_CONVERSATION;
    }

    @Override
    public String initialize(WhatsAppUser whatsAppUser, Map<DialogArgName, String> dialogArgs) {
        String caseRef;
        var openSupportCaseConversation = getOpenSupportCaseConversation(whatsAppUser.getMsisdn());
        if (openSupportCaseConversation == null) {
            caseRef = createSupportCase(whatsAppUser.getMsisdn());
            return String.format("Connecting you to a support consultant. Please wait. Your reference is *%s*",
                    caseRef);
        } else {
            caseRef = openSupportCaseConversation;
            return null;// user is replying
        }
    }

    @Override
    public DialogResult interact(String input, WhatsAppUser whatsAppUser, Map<DialogArgName, String> dialogArgs) {
        // keep them in the convo until the conversation is closed
        return new DialogResult(DialogName.SUPPORT_CONVERSATION, dialogArgs);
    }

    private String createSupportCase(String msisdn) {
        testCasesDb.put(msisdn, "TESTCASE-0001");
        return testCasesDb.get(msisdn);
    }

    private String getOpenSupportCaseConversation(String msisdn) {
        return testCasesDb.get(msisdn);
    }
}
