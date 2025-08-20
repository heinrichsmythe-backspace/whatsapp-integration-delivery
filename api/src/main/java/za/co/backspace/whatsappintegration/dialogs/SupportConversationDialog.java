package za.co.backspace.whatsappintegration.dialogs;

import java.time.LocalDateTime;
import java.util.Map;

import za.co.backspace.whatsappintegration.dialogs.Dialogs.DialogArgName;
import za.co.backspace.whatsappintegration.dialogs.Dialogs.DialogName;
import za.co.backspace.whatsappintegration.dialogs.Dialogs.WhatsAppDialog;
import za.co.backspace.whatsappintegration.integrations.VTigerApiClient;
import za.co.backspace.whatsappintegration.integrations.VTigerApiClient.CreateCaseRequestCaseDetail;
import za.co.backspace.whatsappintegration.persistence.entities.WhatsAppConversation;
import za.co.backspace.whatsappintegration.persistence.entities.WhatsAppUser;
import za.co.backspace.whatsappintegration.persistence.entities.WhatsAppConversation.WhatsAppConversationStatus;
import za.co.backspace.whatsappintegration.persistence.repos.WhatsAppConversationRepository;

public class SupportConversationDialog implements WhatsAppDialog {

    private final WhatsAppConversationRepository whatsAppConvoRepo;
    private final VTigerApiClient vTigerApiClient;

    public SupportConversationDialog(WhatsAppConversationRepository whatsAppConvoRepo,
            VTigerApiClient vTigerApiClient) {
        this.whatsAppConvoRepo = whatsAppConvoRepo;
        this.vTigerApiClient = vTigerApiClient;
    }

    @Override
    public DialogName getDialogName() {
        return DialogName.SUPPORT_CONVERSATION;
    }

    @Override
    public String initialize(WhatsAppUser whatsAppUser, Map<DialogArgName, String> dialogArgs) {
        var openSupportCaseConversation = getOpenSupportCaseConversation(whatsAppUser.getMsisdn());
        if (openSupportCaseConversation == null) {
            var newCase = createSupportCaseAndOpenConversation(whatsAppUser);
            return String.format("Connecting you to a support consultant. Please wait. Your reference is *%s*",
                    newCase.getVTigerCaseNo());
        } else {
            return null;// user is replying
        }
    }

    @Override
    public DialogResult interact(String input, WhatsAppUser whatsAppUser, Map<DialogArgName, String> dialogArgs) {
        // keep them in the convo until the conversation is closed
        return new DialogResult(DialogName.SUPPORT_CONVERSATION, dialogArgs);
    }

    private WhatsAppConversation createSupportCaseAndOpenConversation(WhatsAppUser whatsAppUser) {
        var caseReq = new CreateCaseRequestCaseDetail(
                String.format("💬 WhatsApp Support Convo: %s %s", whatsAppUser.getFirstName(),
                        whatsAppUser.getMsisdn()),
                String.format(
                        "Case opened via inbound WhatsApp support, %s. Use the WhatsApp conversation extension to reply.",
                        whatsAppUser.getMsisdn()),
                "Open",
                "high",
                whatsAppUser.getVTigerContactId());
        var newCase = vTigerApiClient.createCase(caseReq);
        var newConvo = new WhatsAppConversation();
        newConvo.setDateCreated(LocalDateTime.now());
        newConvo.setMsisdn(whatsAppUser.getMsisdn());
        newConvo.setStatus(WhatsAppConversationStatus.OPEN);
        newConvo.setVTigerCaseId(newCase.getId());
        newConvo.setVTigerCaseNo(newCase.getCaseNo());
        newConvo.setVTigerContactId(whatsAppUser.getVTigerContactId());
        whatsAppConvoRepo.save(newConvo);
        return newConvo;
    }

    private WhatsAppConversation getOpenSupportCaseConversation(String msisdn) {
        return whatsAppConvoRepo.findByMsisdnAndStatus(msisdn, WhatsAppConversation.WhatsAppConversationStatus.OPEN);
    }
}
