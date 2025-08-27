package za.co.backspace.whatsappintegration.jobs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import za.co.backspace.whatsappintegration.services.WhatsAppService;

@Component
public class ConversationAutoCloseJob {
    private final Logger logger = LoggerFactory.getLogger(ConversationAutoCloseJob.class);

    @Autowired
    private WhatsAppService whatsAppService;

    @Scheduled(fixedRate = 10000)
    public void runJob() {
        logger.info("Running Autoclose job at " + System.currentTimeMillis());
        whatsAppService.closeInactiveConversations();
    }
}
