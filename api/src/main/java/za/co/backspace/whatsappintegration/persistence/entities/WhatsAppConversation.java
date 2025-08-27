package za.co.backspace.whatsappintegration.persistence.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
public class WhatsAppConversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String msisdn;
    LocalDateTime dateCreated;
    String contactId;
    String caseId;// nullable
    String caseNo;// nullable
    WhatsAppConversationStatus status;
    String closedBy;
    LocalDateTime dateClosed;
    LocalDateTime lastActivity;

    public enum WhatsAppConversationStatus {
        OPEN,
        CLOSED
    }
}
