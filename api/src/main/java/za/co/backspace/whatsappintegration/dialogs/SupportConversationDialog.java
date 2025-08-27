package za.co.backspace.whatsappintegration.dialogs;

import java.util.Map;

import za.co.backspace.whatsappintegration.dialogs.Dialogs.DialogArgName;
import za.co.backspace.whatsappintegration.dialogs.Dialogs.DialogName;
import za.co.backspace.whatsappintegration.dialogs.Dialogs.WhatsAppDialog;
import za.co.backspace.whatsappintegration.persistence.entities.WhatsAppConversation;
import za.co.backspace.whatsappintegration.persistence.entities.WhatsAppUser;
import za.co.backspace.whatsappintegration.services.WhatsAppService;

public class SupportConversationDialog implements WhatsAppDialog {

    private final WhatsAppService whatsAppService;

    public SupportConversationDialog(WhatsAppService whatsAppService) {
        this.whatsAppService = whatsAppService;
    }

    @Override
    public DialogName getDialogName() {
        return DialogName.SUPPORT_CONVERSATION;
    }

    @Override
    public String initialize(WhatsAppUser whatsAppUser, Map<DialogArgName, String> dialogArgs) {
        var openSupportCaseConversation = getOpenSupportCaseConversation(whatsAppUser.getMsisdn());
        if (openSupportCaseConversation == null) {
            var title = String.format("💬 WhatsApp Support Convo: %s %s", whatsAppUser.getFirstName(),
                    whatsAppUser.getMsisdn());
            var description = String.format(
                    "Case opened via inbound WhatsApp support, %s. Use the WhatsApp conversation extension to reply.",
                    whatsAppUser.getMsisdn());
            var newCase = whatsAppService.createCaseAndOpenConversation(whatsAppUser, title, description);
            return String.format("Connecting you to a support consultant. Please wait. Your reference is *%s*",
                    newCase.getCaseNo());
        } else {
            return null;// user is replying
        }
    }

    @Override
    public DialogResult interact(String input, WhatsAppUser whatsAppUser, Map<DialogArgName, String> dialogArgs) {
        // keep them in the convo until the conversation is closed
        var openSupportCaseConversation = getOpenSupportCaseConversation(whatsAppUser.getMsisdn());
        whatsAppService.createIncomingMessageOnCase(openSupportCaseConversation.getId(),
                openSupportCaseConversation.getCaseId(), input, whatsAppUser);
        return new DialogResult(DialogName.SUPPORT_CONVERSATION, dialogArgs);
    }

    private WhatsAppConversation getOpenSupportCaseConversation(String msisdn) {
        return whatsAppService.getOpenCaseConversationForMsisdn(msisdn);
    }
}
