package za.co.backspace.whatsappintegration.jobs;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ConversationCloserJob {

    // Runs every 10 seconds
    @Scheduled(fixedRate = 60000)
    public void runJob() {
        System.out.println("Running background job at " + System.currentTimeMillis());
    }
}
