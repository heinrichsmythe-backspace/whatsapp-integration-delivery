<script setup lang="ts">
import { computed, nextTick, onMounted, ref, watch } from 'vue';
import ErrorHandler from './errorhandling/ErrorHandler';
import ConversationService, { type WhatsAppConversationFullInfo } from './services/ConversationService';
import LoadingSpinner from './components/LoadingSpinner.vue';
import DateFormatter from './formatters/DateFormatter';
import { TOKEN_KEY } from './localStorage/keys';
import { setToken } from './http/axiosClient';

const authToken = ref<string>();
const currentCaseId = ref<string>();
const loading_converstation = ref<boolean>();
const loading_sendMessage = ref<boolean>();
const loading_closingConversation = ref<boolean>();
const caseConvo = ref<WhatsAppConversationFullInfo>();
const input_message = ref<string>();
const chatContainer = ref<HTMLElement | null>(null);
const chromeTabId = ref<number>();
const loading_auth = ref<boolean>();
const input_username = ref<string>();
const input_accessKey = ref<string>();

onMounted(() => {
  chrome.storage.local.get(TOKEN_KEY).then(res => {
    if (res) {

      authToken.value = res[TOKEN_KEY];
    }
  });
});

watch(() => authToken.value, (newValue, prevValue) => {
  if (prevValue != newValue) {
    if (newValue) {
      setToken(newValue);
      //user authed success
      initAfterAuth();
    }
  }
});

watch(() => currentCaseId.value, (newValue, previousValue) => {
  if (newValue && previousValue != newValue) {
    fetchConversationForCase();
  }
});

const authButtonDisabled = computed(() => {
  if (loading_auth.value) {
    return true;
  }
  if (!input_accessKey.value || input_accessKey.value.length == 0) {
    return true;
  }
  if (!input_username.value || input_username.value.length == 0) {
    return true;
  }
});

const initAfterAuth = () => {
  chrome.tabs.query({ active: true, currentWindow: true }, (tabs) => {
    chromeTabId.value = tabs[0]?.id;
    if (!chromeTabId) return;

    // 2️⃣ Ask background for last case for this tab
    chrome.runtime.sendMessage({ type: "GET_LAST_CASE", tabId: chromeTabId.value }, (resp) => {
      if (resp?.caseId) currentCaseId.value = resp.caseId;
    });
  });

  // 2️⃣ Listen for new case messages while popup is open
  chrome.runtime.onMessage.addListener((msg) => {
    if (msg.type === "CASE_OPENED") {
      currentCaseId.value = msg.caseId;
    }
  });
}

const fetchConversationForCase = () => {
  loading_converstation.value = true;
  caseConvo.value = undefined;
  ConversationService.getConversationForCase(currentCaseId.value!).then(success => {
    loading_converstation.value = false;
    if (success.data) {
      caseConvo.value = success.data;
      scrollToBottomOfChat();
      let lastMessageId: string | undefined;
      if (caseConvo.value.messages.length > 0) {
        lastMessageId = caseConvo.value.messages[caseConvo.value.messages.length - 1].id;
      }
      chrome.runtime.sendMessage({ type: "START_POLLING_FOR_NEW_MESSAGES", tabId: chromeTabId.value, lastMessageId });
    }
  }, error => {
    ErrorHandler.handleApiErrorResponse(error);
    loading_converstation.value = false;
  });
}

const sendMessage = () => {
  if (!input_message.value) {
    return;
  }
  if (!currentCaseId.value) {
    return;
  }
  loading_sendMessage.value = true;
  ConversationService.sendMessage(currentCaseId.value, input_message.value).then(success => {
    caseConvo.value = success.data;
    loading_sendMessage.value = false;
    input_message.value = undefined;
    scrollToBottomOfChat();
  }, error => {
    ErrorHandler.handleApiErrorResponse(error);
    loading_sendMessage.value = false;
  });
}

const scrollToBottomOfChat = () => {
  nextTick(() => {
    if (chatContainer.value) {
      chatContainer.value.scrollTo({
        top: chatContainer.value.scrollHeight,
        behavior: "smooth",
      })
    }
  });
}

const recheckForCase = () => {
  chrome.runtime.sendMessage({ type: "RECHECK_FOR_CASE_IN_CONTENT" });
}

const closeConversation = () => {
  if (!currentCaseId.value) {
    return;
  }
  const res = confirm('Are you sure you want to close this conversation?');
  if (res) {
    loading_closingConversation.value = true;
    ConversationService.closeConverstation(currentCaseId.value).then(success => {
      caseConvo.value = success.data;
      loading_closingConversation.value = false;
      input_message.value = undefined;
    }, error => {
      ErrorHandler.handleApiErrorResponse(error);
      loading_closingConversation.value = false;
    });
  }
}

const tryAuthenticate = () => {
  if (!input_username.value) {
    return;
  }
  if (!input_accessKey.value) {
    return;
  }
  loading_auth.value = true;
  ConversationService.tryAuth(input_username.value, input_accessKey.value).then(success => {
    loading_auth.value = false;
    const t = {
      vttoken: success.data.token
    };
    chrome.storage.local.set(t);
    authToken.value = success.data.token;
    input_username.value = undefined;
    input_accessKey.value = undefined;
  }, error => {
    ErrorHandler.handleApiErrorResponse(error);
    loading_auth.value = false;
  });
}

const logout = async () => {
  await chrome.storage.local.remove(TOKEN_KEY);
  authToken.value = undefined;
}

</script>

<template>
  <div class="py-3 px-3">
    <div v-if="authToken">
      <div class="">
        <div class="row">
          <div class="col-6">
            <div v-if="caseConvo">
              <i class="icon wb-chat-text" aria-hidden="true"></i> Case Chat: {{ caseConvo.caseNo }}
            </div>
          </div>
          <div class="col-6 text-end">
            <button class="btn btn-icon" @click="recheckForCase()"><i class="fa fa-refresh"></i></button>
            <button class="btn btn-icon" @click="logout()"><i class="fa fa-sign-out"></i></button>
          </div>
        </div>
      </div>
      <div v-if="currentCaseId">
        <hr />
        <LoadingSpinner :loading="loading_converstation || loading_closingConversation" class="text-center mt-2" />
        <div class="">
          <div class="row" v-if="caseConvo">
            <div class="col-12">
              <button class="btn btn-outline-danger float-end" v-if="caseConvo.status == 'OPEN'"
                @click="closeConversation()">Close conversation</button>
            </div>
            <div class="col-md-7 col-xs-12 col-md-offset-2">
              <div class="panel">
                <div class="panel-heading">
                  <p class="panel-title">
                    <div>Conversation with: {{caseConvo.contactName}} ({{ caseConvo.msisdn }})</div>
                  </p>
                </div>
                <div class="panel-body" ref="chatContainer">
                  <div class="chats">
                    <p class="text-center text-muted" v-if="caseConvo.messages.length == 0">No messages yet</p>
                    <div class="chat" :class="{ 'chat-left': message.direction == 'Incoming' }"
                      v-for="message in caseConvo.messages">
                      <div class="chat-body">
                        <div class="chat-content">
                          <p>
                            {{ message.messageText }}
                          </p>
                          <small class="chat-time mb-0">{{ DateFormatter.formatDateTime(message.date) }}</small>
                          <small>{{ message.author }}</small>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
                <div class="panel-footer">
                  <p v-if="caseConvo.status == 'CLOSED'">Conversation was closed by {{ caseConvo.closedBy }} @ {{
                    DateFormatter.formatDateTime(caseConvo.dateClosed!) }}</p>
                  <div class="input-group" v-if="caseConvo.status == 'OPEN'">
                    <textarea type="text" class="form-control" placeholder="Say something" rows="2"
                      v-model="input_message"></textarea>
                    <span class="input-group-btn">
                      <button class="btn btn-primary" type="button" @click="sendMessage()"
                        :disabled="!input_message || input_message.length == 0 || loading_sendMessage || loading_closingConversation">Send</button>
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div v-else>
            No Conversation is open for this Case
          </div>
        </div>
      </div>
      <div v-else>
        <p class="">No case selected</p>
        <p>Open a case on vTiger</p>
      </div>
    </div>
    <div v-else>
      <!-- <p><i class="fa fa-unlock text-warning me-1"></i>Authentication is required</p> -->
      <p class="text-muted">Enter username and access key</p>
      <div class="form-group">
        <input placeholder="vTiger username" class="form-control mb-2" v-model="input_username" />
      </div>
      <div class="form-group">
        <input placeholder="vTiger access key" class="form-control mb-2" type="password" v-model="input_accessKey" />
        <div class="mb-2">
          <small>Access key can be found on the "My Preferences" page in vTiger</small>
        </div>
      </div>
      <button class="btn btn-primary" @click="tryAuthenticate()" :disabled="authButtonDisabled">Authenticate</button>
    </div>
  </div>
</template>

<style scoped>
.panel-body {
  min-height: 340px;
  max-height: 340px;
  /* optional */
  overflow-y: auto;
}

.chat-box {
  height: 100%;
  width: 100%;
  background-color: #fff;
  overflow: hidden
}

.chats {
  padding: 30px 15px
}

.chat-avatar {
  float: right
}

.chat-avatar .avatar {
  width: 30px;
  -webkit-box-shadow: 0 2px 2px 0 rgba(0, 0, 0, 0.2), 0 6px 10px 0 rgba(0, 0, 0, 0.3);
  box-shadow: 0 2px 2px 0 rgba(0, 0, 0, 0.2), 0 6px 10px 0 rgba(0, 0, 0, 0.3);
}

.chat-body {
  display: block;
  margin: 6px 6px 6px 6px;
  overflow: hidden
}

.chat-body:first-child {
  margin-top: 0
}

.chat-content {
  position: relative;
  display: block;
  float: right;
  padding: 8px 15px;
  margin: 6px 6px 6px 6px;
  clear: both;
  color: #fff;
  background-color: #62a8ea;
  border-radius: 4px;
  -webkit-box-shadow: 0 1px 4px 0 rgba(0, 0, 0, 0.37);
  box-shadow: 0 1px 4px 0 rgba(0, 0, 0, 0.37);
}

.chat-content:before {
  position: absolute;
  top: 10px;
  right: -10px;
  width: 0;
  height: 0;
  content: '';
  border: 5px solid transparent;
  border-left-color: #62a8ea
}

.chat-content>p:last-child {
  margin-bottom: 0
}

.chat-content+.chat-content:before {
  border-color: transparent
}

.chat-time {
  display: block;
  margin-top: 8px;
  color: rgba(255, 255, 255, .6)
}

.chat-left .chat-avatar {
  float: left
}

.chat-left .chat-body {
  margin-right: 0;
  /* margin-left: 30px */
}

.chat-left .chat-content {
  float: left;
  color: #76838f;
  background-color: #dfe9ef
}

.chat-left .chat-content:before {
  right: auto;
  left: -10px;
  border-right-color: #dfe9ef;
  border-left-color: transparent
}

.chat-left .chat-content+.chat-content:before {
  border-color: transparent
}

.chat-left .chat-time {
  color: #a3afb7
}

.panel-footer {
  padding: 0 30px 15px;
  background-color: transparent;
  border-top: 1px solid transparent;
  border-bottom-right-radius: 3px;
  border-bottom-left-radius: 3px;
}

.avatar img {
  width: 100%;
  max-width: 100%;
  height: auto;
  border: 0 none;
  border-radius: 1000px;
}

.chat-avatar .avatar {
  width: 30px;
}

.avatar {
  position: relative;
  display: inline-block;
  width: 40px;
  white-space: nowrap;
  border-radius: 1000px;
  vertical-align: bottom;
}
</style>
