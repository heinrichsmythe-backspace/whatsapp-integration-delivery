package za.co.backspace.whatsappintegration.integrations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import za.co.backspace.whatsappintegration.config.WhatsAppIntegrationApplicationConfig;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

@Component
public class SlackApiClient {
    
    private final Logger logger = LoggerFactory.getLogger(SlackApiClient.class);

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final WhatsAppIntegrationApplicationConfig config;

    public SlackApiClient(WhatsAppIntegrationApplicationConfig config) {
        this.config = config;
    }

    public void sendMessageToSlack(String channel, String message) {
        try {
            if (config.getSlackWebhookUrl().equals("DISABLED")) {
                logger.warn("Slack is disabled (no webhook or token configured). Message: {}", message);
                return;
            }

            Map<String, Object> payload = new HashMap<>();
            payload.put("channel", channel);
            payload.put("text", message);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String json = objectMapper.writeValueAsString(payload);
            HttpEntity<String> request = new HttpEntity<>(json, headers);
            ResponseEntity<String> response;
            response = restTemplate.postForEntity(config.getSlackWebhookUrl(), request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("Message successfully sent to Slack.");
            } else {
                logger.error("Failed to send Slack message: HTTP {}", response.getStatusCode());
            }

        } catch (Exception e) {
            logger.error("Error sending message to Slack", e);
        }
    }
}
