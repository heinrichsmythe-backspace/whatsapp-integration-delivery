package za.co.backspace.whatsappintegration.dialogs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import za.co.backspace.whatsappintegration.dialogs.Dialogs.DialogArgName;
import za.co.backspace.whatsappintegration.dialogs.Dialogs.DialogName;
import za.co.backspace.whatsappintegration.dialogs.Dialogs.WhatsAppDialog;
import za.co.backspace.whatsappintegration.integrations.VTigerApiClient;
import za.co.backspace.whatsappintegration.integrations.VTigerApiClient.CreateCaseRequestCaseDetail;
import za.co.backspace.whatsappintegration.persistence.entities.WhatsAppUser;
import za.co.backspace.whatsappintegration.persistence.repos.WhatsAppConversationRepository;

public class CallMeBackDialog implements WhatsAppDialog {

    private VTigerApiClient vTigerApiClient;

    public CallMeBackDialog(VTigerApiClient vTigerApiClient) {
        this.vTigerApiClient = vTigerApiClient;
    }

    @Override
    public DialogName getDialogName() {
        return DialogName.CALL_ME_BACK;
    }

    @Override
    public String initialize(WhatsAppUser whatsAppUser, Map<DialogArgName, String> dialogArgs) {
        var caseReq = new CreateCaseRequestCaseDetail(
                String.format("💬 WhatsApp Call Me Back: %s %s", whatsAppUser.getFirstName(),
                        whatsAppUser.getMsisdn()),
                String.format(
                        "Case opened via inbound WhatsApp Call me back, %s.",
                        whatsAppUser.getMsisdn()),
                "Open",
                "high",
                whatsAppUser.getVTigerContactId());
        var newCase = vTigerApiClient.createCase(caseReq);
        List<String> menu = new ArrayList<>();
        menu.add(String.format("Sure thing!\n\nA team member will call you back shortly, your reference is %s\n",
                newCase.getCaseNo()));
        menu.add("0. Back to main menu\n");
        return String.join("\n", menu);
    }

    @Override
    public DialogResult interact(String input, WhatsAppUser whatsAppUser, Map<DialogArgName, String> dialogArgs) {
        return new DialogResult(DialogName.MAIN_MENU, Collections.emptyMap());
    }
}
