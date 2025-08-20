package za.co.backspace.whatsappintegration.persistence.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import za.co.backspace.whatsappintegration.persistence.entities.WhatsAppUser;

public interface WhatsAppUserRepository extends JpaRepository<WhatsAppUser, Long> {

    WhatsAppUser findByMsisdn(String msisdn);
}
