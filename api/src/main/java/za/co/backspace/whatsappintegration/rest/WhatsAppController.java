package za.co.backspace.whatsappintegration.rest;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import za.co.backspace.whatsappintegration.dtos.ApiResponse;
import za.co.backspace.whatsappintegration.dtos.WhatsAppConversationDtos.WhatsAppConversationFullInfo;
import za.co.backspace.whatsappintegration.dtos.WhatsAppConversationDtos.WhatsAppConversationMessage;
import za.co.backspace.whatsappintegration.dtos.whatsapp.WhatsAppCallbackPayload;
import za.co.backspace.whatsappintegration.persistence.entities.WhatsAppConversation.WhatsAppConversationStatus;
import za.co.backspace.whatsappintegration.services.WhatsAppService;

@RestController("whatsapp")
public class WhatsAppController {

    @Autowired
    private WhatsAppService whatsAppService;

    private String verifyToken = "whatsappintegrationapplication";

    @GetMapping("/verify")
    public ResponseEntity<String> verify(
            @RequestParam(name = "hub.mode", required = false) String mode,
            @RequestParam(name = "hub.verify_token", required = false) String token,
            @RequestParam(name = "hub.challenge", required = false) String challenge) {
        if ("subscribe".equals(mode) && verifyToken.equals(token)) {
            return ResponseEntity.ok(challenge);
        } else {
            return ResponseEntity.status(403).body("Forbidden");
        }
    }

    @PostMapping("conversations/callback")
    ResponseEntity<List<String>> handleCallback(@RequestBody WhatsAppCallbackPayload.WhatsAppWebhookPayload body) {
        var whatsAppMessage = body.getEntry().get(0).getChanges().get(0).getValue().getMessages().get(0);
        var res = whatsAppService.handleIncomingMessage(whatsAppMessage);
        return ResponseEntity.ok(res);
    }

    @GetMapping("vtiger/conversations/case/{caseId}")
    public ApiResponse<WhatsAppConversationFullInfo> getConvoForVTigerCase(@PathVariable("caseId") String caseId) {
        var messages = List.of(
                new WhatsAppConversationMessage("1", "incoming", "Simon Support",
                        LocalDateTime.of(2025, 8, 24, 10, 1, 0),
                        "Hello, Collin, it's Simon from support, how can I help you today?"),
                new WhatsAppConversationMessage(
                        "2",
                        "outgoing",
                        "Collin Customer",
                        LocalDateTime.of(2025, 8, 24, 10, 0, 0),
                        "hi support, my vop lin brokn"));
        var info = new WhatsAppConversationFullInfo(caseId, "CASE123", WhatsAppConversationStatus.OPEN, messages,
                null, null);
        return new ApiResponse<>("Get convo success", info);
    }

    @PostMapping("vtiger/conversations/case/{caseId}/messsages/send")
    public ApiResponse<WhatsAppConversationFullInfo> sendMessage(@PathVariable("caseId") String caseId,
            @RequestBody SendMessageRequest sendMessageRequest) {
        var newm = new WhatsAppConversationMessage(
                "1",
                "outgoing", "You", LocalDateTime.now(), sendMessageRequest.message());
        var messages = List.of(
                new WhatsAppConversationMessage("1", "incoming", "Simon Support",
                        LocalDateTime.of(2025, 8, 24, 10, 1, 0),
                        "Hello, Collin, it's Simon from support, how can I help you today?"),
                new WhatsAppConversationMessage(
                        "2",
                        "outgoing",
                        "Collin Customer",
                        LocalDateTime.of(2025, 8, 24, 10, 0, 0),
                        "hi support, my vop lin brokn"),
                newm);
        var info = new WhatsAppConversationFullInfo(caseId, "CASE123", WhatsAppConversationStatus.OPEN, messages,
                "Peter", LocalDateTime.now());
        return new ApiResponse<>("Message sent succesfully", info);
    }

    @PostMapping("vtiger/conversations/case/{caseId}/close")
    public ApiResponse<WhatsAppConversationFullInfo> closeConverstation(@PathVariable("caseId") String caseId) {
        var messages = List.of(
                new WhatsAppConversationMessage("1", "incoming", "Simon Support",
                        LocalDateTime.of(2025, 8, 24, 10, 1, 0),
                        "Hello, Collin, it's Simon from support, how can I help you today?"),
                new WhatsAppConversationMessage(
                        "2",
                        "outgoing",
                        "Collin Customer",
                        LocalDateTime.of(2025, 8, 24, 10, 0, 0),
                        "hi support, my vop lin brokn"));
        var info = new WhatsAppConversationFullInfo(caseId, "CASE123", WhatsAppConversationStatus.CLOSED, messages,
                "Peter", LocalDateTime.now());
        return new ApiResponse<>("Case closed succesfully", info);
    }

    @GetMapping("/vtiger/conversations/case/{caseId}/messages/latest")
    public ApiResponse<WhatsAppConversationMessage> getLatestMessage(@PathVariable("caseId") String caseId) {
        var msg = new WhatsAppConversationMessage(
                "4",
                "incoming", "Joe", LocalDateTime.now(), "message");
        return new ApiResponse<>("Get message succesfully", msg);
    }

    public record SendMessageRequest(String message) {

    }
}
