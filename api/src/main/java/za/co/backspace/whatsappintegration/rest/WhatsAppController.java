package za.co.backspace.whatsappintegration.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import za.co.backspace.whatsappintegration.dtos.ApiResponse;
import za.co.backspace.whatsappintegration.dtos.WhatsAppConversationDtos.WhatsAppConversationFullInfo;
import za.co.backspace.whatsappintegration.dtos.WhatsAppConversationDtos.WhatsAppConversationMessageBasicInfo;
import za.co.backspace.whatsappintegration.dtos.whatsapp.WhatsAppCallbackPayload;
import za.co.backspace.whatsappintegration.integrations.VTigerApiClient.ValidUserAuth;
import za.co.backspace.whatsappintegration.services.WhatsAppService;

@RestController("whatsapp")
public class WhatsAppController {

        @Autowired
        private WhatsAppService whatsAppService;

        private String verifyToken = "whatsappintegrationapplication";

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
        public ApiResponse<WhatsAppConversationFullInfo> getConvoForVTigerCase(
                        @RequestHeader("X-Auth-Token") String token, @PathVariable("caseId") String caseId) {
                var username = whatsAppService.authenticate(token);
                var info = whatsAppService.getConvoForVTigerCase(caseId);
                return new ApiResponse<>("Get convo success", info);
        }

        @PostMapping("vtiger/conversations/case/{caseId}/messages/send")
        public ApiResponse<WhatsAppConversationFullInfo> sendMessage(@RequestHeader("X-Auth-Token") String token,
                        @PathVariable("caseId") String caseId,
                        @RequestBody SendMessageRequest sendMessageRequest) {
                var username = whatsAppService.authenticate(token);
                var info = whatsAppService.sendMessage(username, caseId, sendMessageRequest);
                return new ApiResponse<>("Message sent succesfully", info);
        }

        @PostMapping("vtiger/conversations/case/{caseId}/close")
        public ApiResponse<WhatsAppConversationFullInfo> closeConversation(@RequestHeader("X-Auth-Token") String token,
                        @PathVariable("caseId") String caseId) {
                var username = whatsAppService.authenticate(token);
                var info = whatsAppService.closeConversation(username, caseId);
                return new ApiResponse<>("Conversation closed succesfully", info);
        }

        @GetMapping("/vtiger/conversations/case/{caseId}/messages/latest")
        public ApiResponse<WhatsAppConversationMessageBasicInfo> getLatestMessage(
                        @RequestHeader("X-Auth-Token") String token,
                        @PathVariable("caseId") String caseId) {
                var username = whatsAppService.authenticate(token);
                var latest = whatsAppService.getLatestMessage(caseId);
                return new ApiResponse<>("Get message succesfully", latest);
        }

        @PostMapping("/vtiger/tryauth")
        public ApiResponse<ValidUserAuth> tryVTigerAuth(@RequestBody AuthRequest authRequest) {
                var res = whatsAppService.tryAuthVTiger(authRequest.username(), authRequest.accessKey());
                return new ApiResponse<>("Auth success", res);
        }

        public record SendMessageRequest(String message) {

        }

        public record AuthRequest(String username, String accessKey) {

        }
}
