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
        var info = new WhatsAppConversationFullInfo(caseId, "CASE123", List.of(
                new WhatsAppConversationMessage("incoming", "Simon Support", LocalDateTime.of(2025, 8, 24, 10, 1, 0),
                        "Hello, Collin, it's Simon from support, how can I help you today?"),
                new WhatsAppConversationMessage(
                        "outgoing",
                        "Collin Customer",
                        LocalDateTime.of(2025, 8, 24, 10, 0, 0),
                        "hi support, my vop lin brokn")));
        return new ApiResponse<>("Get convo success", info);
    }
}
