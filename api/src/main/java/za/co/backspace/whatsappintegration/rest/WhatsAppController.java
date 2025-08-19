package za.co.backspace.whatsappintegration.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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
    ResponseEntity<List<String>> handleCallback(@RequestBody WhatsAppWebhookPayload body) {
        var whatsAppMessage = body.entry.get(0).changes.get(0).value.messages.get(0);
        var res = whatsAppService.handleIncomingMessage(whatsAppMessage);
        return ResponseEntity.ok(res);
    }

    public static class WhatsAppWebhookPayload {
        private List<Entry> entry;

        public List<Entry> getEntry() {
            return entry;
        }

        public void setEntry(List<Entry> entry) {
            this.entry = entry;
        }
    }

    public static class Entry {
        private List<Change> changes;

        public List<Change> getChanges() {
            return changes;
        }

        public void setChanges(List<Change> changes) {
            this.changes = changes;
        }
    }

    public static class Change {
        private Value value;

        public Value getValue() {
            return value;
        }

        public void setValue(Value value) {
            this.value = value;
        }
    }

    public static class Value {
        private List<WhatsAppMessage> messages;

        public List<WhatsAppMessage> getMessages() {
            return messages;
        }

        public void setMessages(List<WhatsAppMessage> messages) {
            this.messages = messages;
        }
    }

    public static class WhatsAppMessage {
        private String from;
        private String id;
        private String timestamp;
        private Text text;
        private String type;
        private MediaFile audio;
        private MediaFile image;
        private MediaFile document;

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }

        public Text getText() {
            return text;
        }

        public void setText(Text text) {
            this.text = text;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public MediaFile getAudio() {
            return audio;
        }

        public void setAudio(MediaFile audio) {
            this.audio = audio;
        }

        public MediaFile getImage() {
            return image;
        }

        public void setImage(MediaFile image) {
            this.image = image;
        }

        public MediaFile getDocument() {
            return document;
        }

        public void setDocument(MediaFile document) {
            this.document = document;
        }
    }

    public static class Text {
        private String body;

        public String getBody() {
            return body;
        }

        public void setBody(String body) {
            this.body = body;
        }
    }

    public static class MediaFile {
        private String id;
        private String link;
        private String caption;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getLink() {
            return link;
        }

        public void setLink(String link) {
            this.link = link;
        }

        public String getCaption() {
            return caption;
        }

        public void setCaption(String caption) {
            this.caption = caption;
        }
    }

    public static class WhatsAppMediaFetchResponse {
        private String url;
        private String mimeType;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getMimeType() {
            return mimeType;
        }

        public void setMimeType(String mimeType) {
            this.mimeType = mimeType;
        }
    }
}
