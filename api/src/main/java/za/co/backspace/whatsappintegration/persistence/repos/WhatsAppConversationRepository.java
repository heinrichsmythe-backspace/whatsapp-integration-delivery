package za.co.backspace.whatsappintegration.persistence.repos;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import za.co.backspace.whatsappintegration.persistence.entities.WhatsAppConversation;

public interface WhatsAppConversationRepository extends JpaRepository<WhatsAppConversation, Long> {

    WhatsAppConversation findByMsisdnAndStatus(String msisdn, WhatsAppConversation.WhatsAppConversationStatus status);

    WhatsAppConversation findByCaseId(String caseId);

    List<WhatsAppConversation> findByStatusAndLastActivityBefore(WhatsAppConversation.WhatsAppConversationStatus status,
            LocalDateTime lastActivityCutOff);
}
