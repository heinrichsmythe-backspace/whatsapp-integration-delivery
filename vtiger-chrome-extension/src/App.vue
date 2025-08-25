<script setup lang="ts">
import { onMounted, ref } from 'vue';

const currentCaseId = ref<number>();

onMounted(() => {
  // 1️⃣ Get the last case ID from background
  chrome.runtime.sendMessage({ type: "GET_LAST_CASE" }, (resp) => {
    if (resp?.caseId) currentCaseId.value = resp.caseId;
  });

  // 2️⃣ Listen for new case messages while popup is open
  chrome.runtime.onMessage.addListener((msg) => {
    if (msg.type === "CASE_OPENED") {
      currentCaseId.value = msg.caseId;
    }
  });
});

</script>

<template>
  <div>
    Case: {{ currentCaseId }}
  </div>
</template>

<style scoped>
.logo {
  height: 6em;
  padding: 1.5em;
  will-change: filter;
  transition: filter 300ms;
}

.logo:hover {
  filter: drop-shadow(0 0 2em #646cffaa);
}

.logo.vue:hover {
  filter: drop-shadow(0 0 2em #42b883aa);
}
</style>
