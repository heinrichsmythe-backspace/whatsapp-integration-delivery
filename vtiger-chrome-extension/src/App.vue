<script setup lang="ts">
import { nextTick, onMounted, ref, watch } from 'vue';
import ErrorHandler from './errorhandling/ErrorHandler';
import ConversationService, { type WhatsAppConversationFullInfo } from './services/ConversationService';
import LoadingSpinner from './components/LoadingSpinner.vue';
import DateFormatter from './formatters/DateFormatter';

const currentCaseId = ref<string>();
const loading_converstation = ref<boolean>();
const caseConvo = ref<WhatsAppConversationFullInfo>();
const input_message = ref<string>();
const chatContainer = ref<HTMLElement | null>(null);

onMounted(() => {
  chrome.tabs.query({ active: true, currentWindow: true }, (tabs) => {
    const tabId = tabs[0]?.id;
    if (!tabId) return;

    // 2️⃣ Ask background for last case for this tab
    chrome.runtime.sendMessage({ type: "GET_LAST_CASE", tabId }, (resp) => {
      if (resp?.caseId) currentCaseId.value = resp.caseId;
    });
  });

  // 2️⃣ Listen for new case messages while popup is open
  chrome.runtime.onMessage.addListener((msg) => {
    if (msg.type === "CASE_OPENED") {
      currentCaseId.value = msg.caseId;
    }
  });
});

watch(() => currentCaseId.value, (newValue, previousValue) => {
  console.log(previousValue)
  console.log(newValue)
  if (newValue && previousValue != newValue) {
    fetchConversationForCase();
  }
});

const fetchConversationForCase = () => {
  loading_converstation.value = true;
  ConversationService.getConversationForCase(currentCaseId.value!).then(success => {
    loading_converstation.value = false;
    caseConvo.value = success.data;
  }, error => {
    ErrorHandler.handleApiErrorResponse(error);
    loading_converstation.value = false;
  });
}

const sendMessage = () => {
  if (!input_message.value) {
    return;
  }
  caseConvo.value?.messages.push({
    direction: 'outgoing',
    author: 'me',
    date: '',
    messageText: input_message.value
  });
  input_message.value = undefined;
  nextTick(() => {
    console.log(chatContainer.value)
    if (chatContainer.value) {
      chatContainer.value.scrollTo({
        top: chatContainer.value.scrollHeight,
        behavior: "smooth",
      })
    }
  });
}

</script>

<template>
  <div class="container">
    <div v-if="currentCaseId">
      <LoadingSpinner :loading="loading_converstation" class="text-center mt-2" />
      <div class="">
        <div class="row" v-if="caseConvo">
          <div class="col-md-7 col-xs-12 col-md-offset-2">
            <!-- Panel Chat -->
            <div class="panel">
              <div class="panel-heading">
                <h3 class="panel-title">
                  <i class="icon wb-chat-text" aria-hidden="true"></i> Case Chat: {{ caseConvo.caseNo }}
                </h3>
              </div>
              <div class="panel-body" ref="chatContainer">
                <div class="chats">
                  <div class="chat" :class="{ 'chat-left': message.direction == 'incoming' }"
                    v-for="message in caseConvo.messages">
                    <div class="chat-body">
                      <div class="chat-content">
                        {{ message.author }}
                        <p>
                          {{ message.messageText }}
                        </p>
                        <p class="chat-time">{{ DateFormatter.formatDateTime(message.date) }}</p>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              <div class="panel-footer">
                <div class="input-group">
                  <textarea type="text" class="form-control" placeholder="Say something" rows="2"
                    v-model="input_message"></textarea>
                  <span class="input-group-btn">
                    <button class="btn btn-primary" type="button" @click="sendMessage()"
                      :disabled="!input_message || input_message.length == 0">Send</button>
                  </span>
                </div>
              </div>
            </div>
            <!-- End Panel Chat -->
          </div>
        </div>
      </div>
    </div>
    <div v-else>
      <p class="">No case selected</p>
      <p>Open a case on vTiger</p>
    </div>
  </div>
</template>

<style scoped>
.panel-body {
  min-height: 400px;
  max-height: 400px;
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
