package za.co.backspace.whatsappintegration.utils;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import za.co.backspace.whatsappintegration.dialogs.Dialogs.DialogArgName;
import za.co.backspace.whatsappintegration.dialogs.Dialogs.DialogName;

@Component
public class ConversationCache {

    private static final Map<String, ConversationState> conversationCache = new HashMap<>();

    public ConversationState getByKey(String implementationName, String key) {
        String fullKey = implementationName + ":" + key;
        return conversationCache.getOrDefault(fullKey, null);
    }

    public void update(String implementationName, String key, DialogName dialog,
            Map<DialogArgName, String> args) {
        String fullKey = implementationName + ":" + key;

        if (conversationCache.containsKey(fullKey)) {
            Map<DialogArgName, String> previousArgs = conversationCache.get(fullKey).getArgs();
            Map<DialogArgName, String> allArgs = updateDialogArgs(previousArgs, args);

            ConversationState newState = new ConversationState();
            newState.setDialogName(dialog);
            newState.setArgs(allArgs);

            conversationCache.put(fullKey, newState);
        } else {
            ConversationState newState = new ConversationState();
            newState.setDialogName(dialog);
            newState.setArgs(args);

            conversationCache.put(fullKey, newState);
        }
    }

    private Map<DialogArgName, String> updateDialogArgs(Map<DialogArgName, String> previousArgs,
            Map<DialogArgName, String> args) {
        Map<DialogArgName, String> allArgs = new HashMap<>();

        // copy previous args
        for (Map.Entry<DialogArgName, String> entry : previousArgs.entrySet()) {
            allArgs.put(entry.getKey(), entry.getValue());
        }

        // overwrite with new args
        for (Map.Entry<DialogArgName, String> entry : args.entrySet()) {
            allArgs.put(entry.getKey(), entry.getValue());
        }

        return allArgs;
    }

    public void remove(String implementationName, String key) {
        String fullKey = implementationName + ":" + key;
        conversationCache.remove(fullKey);
    }

    public class ConversationState {

        private DialogName dialogName;
        private Map<DialogArgName, String> args;

        public DialogName getDialogName() {
            return dialogName;
        }

        public void setDialogName(DialogName dialogName) {
            this.dialogName = dialogName;
        }

        public Map<DialogArgName, String> getArgs() {
            return args;
        }

        public void setArgs(Map<DialogArgName, String> args) {
            this.args = args;
        }
    }
}
