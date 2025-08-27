package za.co.backspace.whatsappintegration.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import za.co.backspace.whatsappintegration.SystemInsights;
import za.co.backspace.whatsappintegration.config.WhatsAppIntegrationApplicationConfig;
import za.co.backspace.whatsappintegration.dialogs.Dialogs;
import za.co.backspace.whatsappintegration.dialogs.Dialogs.DialogArgName;
import za.co.backspace.whatsappintegration.dialogs.Dialogs.DialogName;
import za.co.backspace.whatsappintegration.dtos.WhatsAppConversationDtos.WhatsAppConversationFullInfo;
import za.co.backspace.whatsappintegration.dtos.WhatsAppConversationDtos.WhatsAppConversationMessageBasicInfo;
import za.co.backspace.whatsappintegration.dtos.whatsapp.WhatsAppCallbackPayload;
import za.co.backspace.whatsappintegration.integrations.VTigerApiClient;
import za.co.backspace.whatsappintegration.integrations.VTigerApiClient.CreateCaseRequestCaseDetail;
import za.co.backspace.whatsappintegration.integrations.WhatsAppApiClient;
import za.co.backspace.whatsappintegration.persistence.entities.WhatsAppConversation;
import za.co.backspace.whatsappintegration.persistence.entities.WhatsAppConversation.WhatsAppConversationStatus;
import za.co.backspace.whatsappintegration.persistence.entities.WhatsAppConversationMessage;
import za.co.backspace.whatsappintegration.persistence.entities.WhatsAppUser;
import za.co.backspace.whatsappintegration.persistence.repos.WhatsAppConversationMessagesRepository;
import za.co.backspace.whatsappintegration.persistence.repos.WhatsAppConversationRepository;
import za.co.backspace.whatsappintegration.persistence.repos.WhatsAppUserRepository;
import za.co.backspace.whatsappintegration.rest.WhatsAppController.SendMessageRequest;
import za.co.backspace.whatsappintegration.utils.ConversationCache;

import java.time.LocalDateTime;
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
    private WhatsAppConversationMessagesRepository whatsAppConversationMessagesRepository;
    @Autowired
    private WhatsAppUserRepository whatsAppUserRepository;
    @Autowired
    private WhatsAppIntegrationApplicationConfig config;
    @Autowired
    private SystemInsights systemInsights;

    public List<String> handleIncomingMessage(WhatsAppCallbackPayload.WhatsAppMessage payload) {
        var fromMsisdn = payload.getFrom();
        var messagesToSend = getMessagesToSend(fromMsisdn, payload.getText().getBody());
        return messagesToSend;
    }

    private List<String> getMessagesToSend(String fromMsisdn, String messageBody) {
        var defaultDialog = DialogName.MAIN_MENU;
        var messagesToSend = new ArrayList<String>();
        var dialogs = Dialogs.WhatsAppDialogFlow(this, vTigerApiClient);
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

    public WhatsAppConversation createCaseAndOpenConversation(WhatsAppUser whatsAppUser, String caseTitle,
            String caseDescription) {
        var caseReq = new CreateCaseRequestCaseDetail(
                caseTitle,
                caseDescription,
                "Open",
                "high",
                whatsAppUser.getVTigerContactId());
        var newCase = vTigerApiClient.createCase(caseReq);
        var newConvo = new WhatsAppConversation();
        newConvo.setDateCreated(LocalDateTime.now());
        newConvo.setMsisdn(whatsAppUser.getMsisdn());
        newConvo.setStatus(WhatsAppConversationStatus.OPEN);
        newConvo.setCaseId(newCase.getId());
        newConvo.setCaseNo(newCase.getCaseNo());
        newConvo.setContactId(whatsAppUser.getVTigerContactId());
        whatsAppConversationRepository.save(newConvo);
        return newConvo;
    }

    public WhatsAppConversationFullInfo getConvoForVTigerCase(String caseId) {
        caseId = vTigerApiClient.makeModuleCaseId(caseId);
        var convo = whatsAppConversationRepository.findByCaseId(caseId);
        if (convo == null) {
            // a case was created but a convo wasn't opened. like a CallMeBack
            return null;
        }

        List<WhatsAppConversationMessageBasicInfo> messages = whatsAppConversationMessagesRepository
                .findByCaseId(caseId).stream().map(m -> makeBasicInfo(m))
                .toList();
        var info = new WhatsAppConversationFullInfo(
                convo.getCaseId(),
                convo.getCaseNo(),
                convo.getMsisdn(),
                convo.getStatus(),
                messages,
                convo.getClosedBy(),
                convo.getDateClosed());
        return info;
    }

    public WhatsAppConversation getOpenCaseConversationForMsisdn(String msisdn) {
        return whatsAppConversationRepository.findByMsisdnAndStatus(msisdn,
                WhatsAppConversation.WhatsAppConversationStatus.OPEN);
    }

    public WhatsAppConversationFullInfo sendMessage(String userName, String caseId,
            SendMessageRequest sendMessageRequest) {
        caseId = vTigerApiClient.makeModuleCaseId(caseId);
        var convo = whatsAppConversationRepository.findByCaseId(caseId);
        if (convo.getStatus() == WhatsAppConversationStatus.CLOSED) {
            return getConvoForVTigerCase(caseId);
        }
        var newMessage = new WhatsAppConversationMessage();
        newMessage.setConversationId(convo.getId());
        newMessage.setDirection("Outgoing");
        newMessage.setMessageText(sendMessageRequest.message());
        newMessage.setAuthor(userName);
        newMessage.setCaseId(caseId);
        newMessage.setDate(LocalDateTime.now());
        whatsAppApiClient.sendTextMessage(convo.getMsisdn(), sendMessageRequest.message());
        whatsAppConversationMessagesRepository.save(newMessage);
        convo.setLastActivity(LocalDateTime.now());
        whatsAppConversationRepository.save(convo);
        return getConvoForVTigerCase(caseId);
    }

    public WhatsAppConversationFullInfo closeConversation(String closedBy, String caseId) {
        caseId = vTigerApiClient.makeModuleCaseId(caseId);
        var convo = whatsAppConversationRepository.findByCaseId(caseId);
        if (convo.getStatus() == WhatsAppConversationStatus.OPEN) {
            convo.setClosedBy(closedBy);
            convo.setDateClosed(LocalDateTime.now());
            convo.setStatus(WhatsAppConversationStatus.CLOSED);
            whatsAppConversationRepository.save(convo);
            conversationCache.remove(implementationName, convo.getMsisdn());
        }
        return getConvoForVTigerCase(caseId);
    }

    private static WhatsAppConversationMessageBasicInfo makeBasicInfo(WhatsAppConversationMessage message) {
        if (message == null) {
            return null;
        }
        return new WhatsAppConversationMessageBasicInfo(
                message.getId(),
                message.getDirection(),
                message.getAuthor(),
                message.getDate(),
                message.getMessageText());
    }

    public WhatsAppConversationMessageBasicInfo getLatestMessage(String caseId) {
        caseId = vTigerApiClient.makeModuleCaseId(caseId);
        var latest = whatsAppConversationMessagesRepository.findFirstByCaseIdOrderByDateDesc(caseId);
        return makeBasicInfo(latest);
    }

    public void createIncomingMessageOnCase(Long convoId, String caseId, String messageText, WhatsAppUser user) {
        caseId = vTigerApiClient.makeModuleCaseId(caseId);
        var convo = whatsAppConversationRepository.findByCaseId(caseId);
        var newMessage = new WhatsAppConversationMessage();
        newMessage.setConversationId(convo.getId());
        newMessage.setDirection("Incoming");
        newMessage.setMessageText(messageText);
        newMessage.setAuthor(user.getFirstName() + " " + user.getLastName());
        newMessage.setCaseId(caseId);
        newMessage.setDate(LocalDateTime.now());
        whatsAppConversationMessagesRepository.save(newMessage);
        convo.setLastActivity(LocalDateTime.now());
        whatsAppConversationRepository.save(convo);
    }

    public void closeInactiveConversations() {
        LocalDateTime date = LocalDateTime.now().minusSeconds(config.getConversationAutoCloseSeconds());
        List<WhatsAppConversation> toClose = whatsAppConversationRepository
                .findByStatusAndLastActivityBefore(WhatsAppConversationStatus.OPEN, date);
        logger.info(toClose.size() + " conversations to auto close");
        toClose.forEach(c -> {
            try {
                logger.info("Auto Closing conversation # " + c.getId() + " due to inactivity");
                closeConversation("Autoclosed", c.getCaseId());
            } catch (Exception e) {
                systemInsights.reportError(e, "Error closing convo #" + c.getId());
            }
        });
    }
}
