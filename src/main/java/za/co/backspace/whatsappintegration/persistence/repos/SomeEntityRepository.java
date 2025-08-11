package za.co.backspace.whatsappintegration.persistence.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import za.co.backspace.whatsappintegration.persistence.entities.SomeEntity;

public interface SomeEntityRepository extends JpaRepository<SomeEntity, Long> {
}
