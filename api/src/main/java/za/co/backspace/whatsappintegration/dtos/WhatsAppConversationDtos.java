package za.co.backspace.whatsappintegration.dtos;

import java.time.LocalDateTime;
import java.util.List;

public final class WhatsAppConversationDtos {
    public record WhatsAppConversationFullInfo(String caseId, String caseNo,
            List<WhatsAppConversationMessage> messages) {

    }

    public record WhatsAppConversationMessage(String direction, String author, LocalDateTime date, String messageText) {

    }

    private WhatsAppConversationDtos() {
    }
}
