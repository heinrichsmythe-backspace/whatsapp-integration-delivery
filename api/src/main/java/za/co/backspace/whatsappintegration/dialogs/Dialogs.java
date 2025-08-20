package za.co.backspace.whatsappintegration.dialogs;

import java.util.Map;

import za.co.backspace.whatsappintegration.integrations.VTigerApiClient;
import za.co.backspace.whatsappintegration.persistence.entities.WhatsAppUser;
import za.co.backspace.whatsappintegration.persistence.repos.WhatsAppConversationRepository;

public class Dialogs {

    public enum DialogName {
        MAIN_MENU,
        SUPPORT_CONVERSATION,
        SALES_CONVERSATION,
        CALL_ME_BACK,
        END
    }

    public enum DialogArgName {
        SUPPORT_CONVERSATION_CASE_ID
    }

    public static Map<DialogName, WhatsAppDialog> WhatsAppDialogFlow(
            WhatsAppConversationRepository whatsAppConversationRepository, VTigerApiClient vTigerApiClient) {
        Map<DialogName, WhatsAppDialog> dialogs = Map.of(
                DialogName.MAIN_MENU, new MainMenuDialog(),
                DialogName.SUPPORT_CONVERSATION,
                new SupportConversationDialog(whatsAppConversationRepository, vTigerApiClient),
                DialogName.CALL_ME_BACK, new CallMeBackDialog());
        return dialogs;
    }

    public interface WhatsAppDialog {

        DialogName getDialogName();

        String initialize(WhatsAppUser whatsAppUser, Map<DialogArgName, String> dialogArgs);

        DialogResult interact(String input, WhatsAppUser whatsAppUser, Map<DialogArgName, String> dialogArgs);

        default boolean canBeMultiPageMenu() {
            return false;
        }

        default boolean endOfConversationDialog() {
            return false;
        }

        // Wrapper record/class for the tuple result
        class DialogResult {
            private final DialogName dialogName;
            private final Map<DialogArgName, String> nextDialogArgs;

            public DialogResult(DialogName dialogName, Map<DialogArgName, String> nextDialogArgs) {
                this.dialogName = dialogName;
                this.nextDialogArgs = nextDialogArgs;
            }

            public DialogName getDialogName() {
                return dialogName;
            }

            public Map<DialogArgName, String> getNextDialogArgs() {
                return nextDialogArgs;
            }
        }
    }
}
