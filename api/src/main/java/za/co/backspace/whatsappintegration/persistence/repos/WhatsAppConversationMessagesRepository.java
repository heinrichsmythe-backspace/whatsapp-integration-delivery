package za.co.backspace.whatsappintegration.persistence.repos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import za.co.backspace.whatsappintegration.persistence.entities.WhatsAppConversationMessage;

public interface WhatsAppConversationMessagesRepository extends JpaRepository<WhatsAppConversationMessage, Long> {

    List<WhatsAppConversationMessage> findByCaseId(String caseId);
    WhatsAppConversationMessage findFirstByCaseIdOrderByDateDesc(String caseId);
}
