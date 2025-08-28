package za.co.backspace.whatsappintegration.dtos;

import java.time.LocalDateTime;
import java.util.List;

import za.co.backspace.whatsappintegration.persistence.entities.WhatsAppConversation.WhatsAppConversationStatus;

public final class WhatsAppConversationDtos {
    public record WhatsAppConversationFullInfo(String caseId, String caseNo, String msisdn, String contactName, WhatsAppConversationStatus status,
            List<WhatsAppConversationMessageBasicInfo> messages, String closedBy, LocalDateTime dateClosed) {

    }

    public record WhatsAppConversationMessageBasicInfo(Long id, String direction, String author, LocalDateTime date,
            String messageText) {

    }

    private WhatsAppConversationDtos() {
    }
}
