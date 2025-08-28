package za.co.backspace.whatsappintegration.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class WhatsAppIntegrationApplicationConfig {

    @Value("${spring.application.name}")
    private String springApplicationName;

    public String getSpringApplicationName() {
        return springApplicationName;
    }

    @Value("${slack.webhook-url}")
    private String slackWebhookUrl;

    public String getSlackWebhookUrl() {
        return slackWebhookUrl;
    }

    @Value("${slack.error-channel}")
    private String slackErrorChannel;

    public String getSlackErrorChannel() {
        return slackErrorChannel;
    }

    @Value("${whatsapp-api.senderId}")
    private String whatsAppApiSenderId;

    public String getWhatsAppApiSenderId() {
        return whatsAppApiSenderId;
    }

    @Value("${whatsapp-api.accessToken}")
    private String whatsAppApiAccessToken;

    public String getWhatsAppApiAccessToken() {
        return whatsAppApiAccessToken;
    }

    @Value("${vtiger.contacts-id-prefix}")
    private String vTigerContactsIdPrefix;

    public String getVTigerContactsIdPrefix() {
        return vTigerContactsIdPrefix;
    }

    @Value("${vtiger.cases-id-prefix}")
    private String vTigerCasesIdPrefix;

    public String getVTigerCasesIdPrefix() {
        return vTigerCasesIdPrefix;
    }

    @Value("${vtiger.base-url}")
    private String vTigerBaseUrl;

    public String getVTigerBaseUrl() {
        return vTigerBaseUrl;
    }

    @Value("${vtiger.systemuser-username}")
    private String vTigerSystemUsername;

    public String getVTigerSystemUsername() {
        return vTigerSystemUsername;
    }

    @Value("${vtiger.systemuser-accessKey}")
    private String vTigerSystemAccessKey;

    public String getVTigerSystemAccessKey() {
        return vTigerSystemAccessKey;
    }

    @Value("${vtiger.auth-internal-token-key}")
    private String vTigerAuthInternalTokenKey;

    public String getVTigerAuthInternalTokenKey() {
        return vTigerAuthInternalTokenKey;
    }

    @Value("${conversation.autoclose-seconds}")
    private int conversationAutoCloseSeconds;

    public int getConversationAutoCloseSeconds() {
        return conversationAutoCloseSeconds;
    }
}
