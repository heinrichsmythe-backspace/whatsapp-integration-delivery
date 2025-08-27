package za.co.backspace.whatsappintegration.rest;

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
import za.co.backspace.whatsappintegration.dtos.WhatsAppConversationDtos.WhatsAppConversationMessageBasicInfo;
import za.co.backspace.whatsappintegration.dtos.whatsapp.WhatsAppCallbackPayload;
import za.co.backspace.whatsappintegration.services.WhatsAppService;

@RestController("whatsapp")
public class WhatsAppController {

        @Autowired
        private WhatsAppService whatsAppService;

        private String verifyToken = "whatsappintegrationapplication";
        private String tempUserName = "IMPLEMENT THIS";

        @GetMapping("/conversations/callback")
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
                var info = whatsAppService.getConvoForVTigerCase(caseId);
                return new ApiResponse<>("Get convo success", info);
        }

        @PostMapping("vtiger/conversations/case/{caseId}/messages/send")
        public ApiResponse<WhatsAppConversationFullInfo> sendMessage(@PathVariable("caseId") String caseId,
                        @RequestBody SendMessageRequest sendMessageRequest) {
                var info = whatsAppService.sendMessage(tempUserName, caseId, sendMessageRequest);
                return new ApiResponse<>("Message sent succesfully", info);
        }

        @PostMapping("vtiger/conversations/case/{caseId}/close")
        public ApiResponse<WhatsAppConversationFullInfo> closeConversation(@PathVariable("caseId") String caseId) {
                var info = whatsAppService.closeConversation(tempUserName, caseId);
                return new ApiResponse<>("Conversation closed succesfully", info);
        }

        @GetMapping("/vtiger/conversations/case/{caseId}/messages/latest")
        public ApiResponse<WhatsAppConversationMessageBasicInfo> getLatestMessage(
                        @PathVariable("caseId") String caseId) {
                var latest = whatsAppService.getLatestMessage(caseId);
                return new ApiResponse<>("Get message succesfully", latest);
        }

        public record SendMessageRequest(String message) {

        }
}
