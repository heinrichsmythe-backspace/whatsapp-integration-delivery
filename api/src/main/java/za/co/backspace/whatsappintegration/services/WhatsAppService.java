package za.co.backspace.whatsappintegration.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import za.co.backspace.whatsappintegration.dialogs.Dialogs;
import za.co.backspace.whatsappintegration.dialogs.Dialogs.DialogArgName;
import za.co.backspace.whatsappintegration.dialogs.Dialogs.DialogName;
import za.co.backspace.whatsappintegration.integrations.WhatsAppApiClient;
import za.co.backspace.whatsappintegration.persistence.entities.WhatsAppUser;
import za.co.backspace.whatsappintegration.rest.WhatsAppController.WhatsAppMessage;
import za.co.backspace.whatsappintegration.utils.ConversationCache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WhatsAppService {

    private static final String implementationName = "WhatsApp";

    @Autowired
    private WhatsAppApiClient whatsAppApiClient;

    @Autowired
    private ConversationCache conversationCache;

    public List<String> handleIncomingMessage(WhatsAppMessage payload) {
        var fromMsisdn = payload.getFrom();
        var messagesToSend = getMessagesToSend(fromMsisdn, payload.getText().getBody());
        return messagesToSend;
    }

    private List<String> getMessagesToSend(String fromMsisdn, String messageBody) {
        var defaultDialog = DialogName.MAIN_MENU;
        var messagesToSend = new ArrayList<String>();
        var dialogs = Dialogs.WhatsAppDialogFlow();
        var existingConvo = conversationCache.getByKey(implementationName, fromMsisdn);
        DialogName nextDialogName;
        WhatsAppUser whatsAppUser = getOrCreateWhatsAppUserFromMsisdn(fromMsisdn);
        Map<DialogArgName, String> nextDialogArgs = new HashMap<>();
        if (existingConvo != null) {
            var input = messageBody;
            if (input == "0") {
                nextDialogName = defaultDialog;
                nextDialogArgs = Collections.emptyMap();
            }
            else {
                var previousDialogArgs = existingConvo.getArgs();
                var res = dialogs.get(existingConvo.getDialogName()).interact(input, whatsAppUser, previousDialogArgs);
                nextDialogName = res.getDialogName();
                nextDialogArgs = res.getNextDialogArgs();
            }
        } else {
            nextDialogName = defaultDialog;
        }
        if (nextDialogName == DialogName.END) {
            conversationCache.remove(implementationName, fromMsisdn);
            return Collections.emptyList();
        } else {
            var nextDialog = dialogs.get(nextDialogName);
            conversationCache.update(implementationName, fromMsisdn, nextDialogName, nextDialogArgs);
            String templateText = nextDialog.initialize(whatsAppUser, nextDialogArgs);
            if(templateText != null){
                messagesToSend.add(templateText);
                whatsAppApiClient.sendTextMessage(fromMsisdn, templateText);
            }
        }
        return messagesToSend;
    }

    private WhatsAppUser getOrCreateWhatsAppUserFromMsisdn(String msisdn) {
        var wa = new WhatsAppUser();
        wa.setFirstName("peter");
        return wa;
    }
}
