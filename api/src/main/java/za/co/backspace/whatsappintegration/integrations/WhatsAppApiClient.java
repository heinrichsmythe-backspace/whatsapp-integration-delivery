package za.co.backspace.whatsappintegration.integrations;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import za.co.backspace.whatsappintegration.SystemInsights;
import za.co.backspace.whatsappintegration.config.WhatsAppIntegrationApplicationConfig;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class WhatsAppApiClient {

    @Autowired
    private SystemInsights systemInsights;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final WhatsAppIntegrationApplicationConfig config;
    private final Logger logger = LoggerFactory.getLogger(WhatsAppApiClient.class);

    public WhatsAppApiClient(ObjectMapper objectMapper, WhatsAppIntegrationApplicationConfig config) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = objectMapper;
        this.config = config;
    }

    @Async
    public void sendMessagesAsync(String to, List<String> messagesToSend) {
        try {
            for (var messageToSend : messagesToSend) {
                sendTextMessage(to, messageToSend);
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Async
    public void sendTextMessage(String to, String message) {
        String senderId = config.getWhatsAppApiSenderId();
        String accessToken = config.getWhatsAppApiAccessToken();
        if (senderId.equals("DISABLED") || accessToken.equals("DISABLED")) {
            logger.warn("WhatsApp messaging disabled: to: " + to + ", message: " + message);
            return;
        }

        String url = "https://graph.facebook.com/v13.0/" + senderId + "/messages/";

        OutgoingWhatsAppMessage payload = new OutgoingWhatsAppMessage(to, message);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);

            String json = objectMapper.writeValueAsString(payload);
            HttpEntity<String> request = new HttpEntity<>(json, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                systemInsights.reportError(
                        new RuntimeException("WhatsApp API call failed: " + response.getStatusCode()),
                        "Error sending WhatsApp API message");
            }
        } catch (Exception e) {
            systemInsights.reportError(e, "Error sending WhatsApp API message");
        }
    }

    public static class OutgoingWhatsAppMessage {

        @JsonProperty("messaging_product")
        private final String messagingProduct = "whatsapp";

        private String to;
        private String type;
        private TextContent text;

        public OutgoingWhatsAppMessage(String to, String message) {
            this.to = to;
            this.type = "text";
            this.text = new TextContent(message);
        }

        public String getMessagingProduct() {
            return messagingProduct;
        }

        public String getTo() {
            return to;
        }

        public String getType() {
            return type;
        }

        public TextContent getText() {
            return text;
        }

        public static class TextContent {
            private String body;

            public TextContent(String body) {
                this.body = body;
            }

            public String getBody() {
                return body;
            }
        }
    }
}
