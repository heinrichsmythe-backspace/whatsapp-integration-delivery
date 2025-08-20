package za.co.backspace.whatsappintegration.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import za.co.backspace.whatsappintegration.dialogs.Dialogs;
import za.co.backspace.whatsappintegration.dialogs.Dialogs.DialogArgName;
import za.co.backspace.whatsappintegration.dialogs.Dialogs.DialogName;
import za.co.backspace.whatsappintegration.integrations.VTigerApiClient;
import za.co.backspace.whatsappintegration.integrations.WhatsAppApiClient;
import za.co.backspace.whatsappintegration.persistence.entities.WhatsAppUser;
import za.co.backspace.whatsappintegration.persistence.repos.WhatsAppConversationRepository;
import za.co.backspace.whatsappintegration.persistence.repos.WhatsAppUserRepository;
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
    private final Logger logger = LoggerFactory.getLogger(WhatsAppService.class);

    @Autowired
    private WhatsAppApiClient whatsAppApiClient;
    @Autowired
    private VTigerApiClient vTigerApiClient;
    @Autowired
    private ConversationCache conversationCache;
    @Autowired
    private WhatsAppConversationRepository whatsAppConversationRepository;
    @Autowired
    private WhatsAppUserRepository whatsAppUserRepository;

    public List<String> handleIncomingMessage(WhatsAppMessage payload) {
        var fromMsisdn = payload.getFrom();
        var messagesToSend = getMessagesToSend(fromMsisdn, payload.getText().getBody());
        return messagesToSend;
    }

    private List<String> getMessagesToSend(String fromMsisdn, String messageBody) {
        var defaultDialog = DialogName.MAIN_MENU;
        var messagesToSend = new ArrayList<String>();
        var dialogs = Dialogs.WhatsAppDialogFlow(whatsAppConversationRepository, vTigerApiClient);
        var existingConvo = conversationCache.getByKey(implementationName, fromMsisdn);
        DialogName nextDialogName;
        WhatsAppUser whatsAppUser = getOrCreateWhatsAppUserFromMsisdn(fromMsisdn);
        Map<DialogArgName, String> nextDialogArgs = new HashMap<>();
        if (existingConvo != null) {
            var input = messageBody;
            if (input == "0") {
                nextDialogName = defaultDialog;
                nextDialogArgs = Collections.emptyMap();
            } else {
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
            if (templateText != null) {
                messagesToSend.add(templateText);
                whatsAppApiClient.sendTextMessage(fromMsisdn, templateText);
            }
        }
        return messagesToSend;
    }

    private void logConversationEvent(String msisdn, String message) {
        logger.info(String.format("Conversation %s: %s", msisdn, message));
    }

    private WhatsAppUser getOrCreateWhatsAppUserFromMsisdn(String msisdn) {
        var user = whatsAppUserRepository.findByMsisdn(msisdn);
        if (user == null) {
            logConversationEvent(msisdn, "WhatsAppUser doesnt exist: checking vTiger for contact matching msisdn");
            String firstName, lastName, vTigerContactId;
            // see if we can find a contact in vtiger
            var matchingContacts = vTigerApiClient.lookupContactByMsisdn(msisdn);
            logConversationEvent(msisdn,
                    String.format("%d contacts found for msisdn in vTiger", matchingContacts.size()));
            if (matchingContacts.size() > 0) {
                firstName = matchingContacts.get(0).getFirstName();
                lastName = matchingContacts.get(0).getLastName();
                vTigerContactId = matchingContacts.get(0).getId();
            } else {
                firstName = "Unknown";
                lastName = "Unknown";
                logConversationEvent(msisdn, String.format("Creating vTiger contact %s, %s, %s", firstName, lastName,
                        msisdn));
                var newContactReq = new VTigerApiClient.CreateContactRequestContactDetail(
                        firstName,
                        lastName,
                        msisdn);
                var newContact = vTigerApiClient.createContact(newContactReq);
                vTigerContactId = newContact.getId();
            }
            logConversationEvent(msisdn, String.format("Creating WhatsApp user %s, %s, %s, %s", firstName, lastName,
                    msisdn, vTigerContactId));
            user = new WhatsAppUser();
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setMsisdn(msisdn);
            user.setVTigerContactId(vTigerContactId);
            whatsAppUserRepository.save(user);
        } else {
            logConversationEvent(msisdn, String.format("WhatsAppUser %d successfully found for msisdn", user.getId()));
        }
        return user;
    }
}
