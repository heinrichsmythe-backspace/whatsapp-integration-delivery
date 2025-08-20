package za.co.backspace.whatsappintegration.persistence.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import za.co.backspace.whatsappintegration.persistence.entities.WhatsAppConversation;

public interface WhatsAppConversationRepository extends JpaRepository<WhatsAppConversation, Long> {
}
