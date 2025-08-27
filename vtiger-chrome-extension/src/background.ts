/// <reference types="chrome"/>

const apiBaseUrl = `http://localhost:8080`;

chrome.runtime.onInstalled.addListener(() => {
    console.log("Extension installed!");
});

const casesByTab: Record<number, { caseId: number, lastMessageId?: number, pollIntervalId?: number }> = {};

chrome.runtime.onMessage.addListener((msg, sender, sendResponse) => {
    console.log('bg msg: ' + msg.type)

    if (msg.type === "CASE_OPENED") {
        const tabId = sender.tab?.id;
        if (!tabId) return;
        casesByTab[tabId] = { caseId: msg.caseId };
    }

    if (msg.type === "GET_LAST_CASE" && msg.tabId != null) {
        const tabId = msg.tabId;
        if (!tabId) return;
        sendResponse({ caseId: casesByTab[tabId].caseId || null });
    }

    if (msg.type === "RECHECK_FOR_CASE_IN_CONTENT") {
        // Forward it to the content script
        chrome.tabs.query({ active: true, currentWindow: true }, (tabs) => {
            chrome.tabs.sendMessage(tabs[0].id!, { type: "RECHECK_FOR_CASE_IN_CONTENT", text: msg.text });
        });
    }

    if (msg.type === 'START_POLLING_FOR_NEW_MESSAGES') {
        const lastMessageId = msg.lastMessageId;
        const tabId = msg.tabId;
        chrome.tabs.sendMessage(tabId, { type: 'STOP_FLASHING_TAB' });
        const caseForTab = casesByTab[tabId];
        caseForTab.lastMessageId = lastMessageId;
        if (caseForTab.pollIntervalId) {
            clearInterval(caseForTab.pollIntervalId);
        }
        // caseForTab.pollIntervalId = setInterval(() => {
        //     pollApiForLatestMessage(tabId, caseForTab.caseId, caseForTab.lastMessageId);
        // }, 5000);
    }
});

async function pollApiForLatestMessage(tabId: number, caseId: number, lastMessageId?: number) {
    try {
        console.log(`POLLING for last message, case: ${caseId} ${lastMessageId}`);
        const response = await fetch(`${apiBaseUrl}/vtiger/conversations/case/${caseId}/messages/latest`);
        if (!response.ok) throw new Error("Network response was not ok");

        const data = await response.json();
        console.log("API Response:", data);
        console.log(`Message id from API: ${data.data.id}, Last message seen: ${lastMessageId}`);
        if (data.data.id != lastMessageId) {
            console.log('Message id not the same, flash the tab')
            chrome.tabs.sendMessage(tabId, { type: "START_FLASHING_TAB" });
        } else {
            console.log('Message id is the same, dont flash the tab');
        }
    } catch (err) {
        console.error("Polling error:", err);
    }
}