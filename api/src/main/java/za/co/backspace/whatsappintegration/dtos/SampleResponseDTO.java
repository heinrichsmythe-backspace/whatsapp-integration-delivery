package za.co.backspace.whatsappintegration.dtos;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public class SampleResponseDTO {
    Boolean success;
}
