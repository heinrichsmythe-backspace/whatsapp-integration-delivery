package za.co.backspace.whatsappintegration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import za.co.backspace.whatsappintegration.config.WhatsAppIntegrationApplicationConfig;
import za.co.backspace.whatsappintegration.integrations.SlackApiClient;

@Component
public class SystemInsights {

    @Autowired
    private SlackApiClient slackApiClient;

    private final Logger logger = LoggerFactory.getLogger(SystemInsights.class);

    private final WhatsAppIntegrationApplicationConfig config;

    public SystemInsights(WhatsAppIntegrationApplicationConfig config) {
        this.config = config;
    }

    public void reportError(String message, boolean sendToSlack) {
        logger.error(message);

        if (sendToSlack) {
            String errorMessage = String.format(
                    "Error on %s:\n %s", config.getSpringApplicationName(), message != null ? message : "").trim();

            try {
                slackApiClient.sendMessageToSlack(
                        config.getSlackErrorChannel(),
                        errorMessage);
            } catch (Exception e) {
                logger.error("Failed to send error to Slack", e);
            }
        }
    }

    public void reportError(String message) {
        reportError(message, true);
    }

    public void reportError(Exception exception, String context) {
        String message = context + " -> " + exception.getMessage();
        logger.error(message, exception);
        reportError(message, true);
    }
}
