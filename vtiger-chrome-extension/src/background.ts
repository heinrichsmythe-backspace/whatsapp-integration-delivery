/// <reference types="chrome"/>

const apiBaseUrl = `http://localhost:8080`;

chrome.runtime.onInstalled.addListener(() => {
    console.log("Extension installed!");
});

const casesByTab: Record<number, { caseId: number, lastIncomingMessageId?: number, pollIntervalId?: number }> = {};

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
        const lastIncomingMessageId = msg.lastIncomingMessageId;
        chrome.tabs.query({ active: true, currentWindow: true }, (tabs) => {
            const tabId = tabs[0].id!;
            chrome.tabs.sendMessage(tabId, { type: 'STOP_FLASHING_TAB' });
            const caseForTab = casesByTab[tabId];
            caseForTab.lastIncomingMessageId = lastIncomingMessageId;
            if (caseForTab.pollIntervalId) {
                clearInterval(caseForTab.pollIntervalId);
            }
            caseForTab.pollIntervalId = setInterval(() => {
                pollApiForLatestMessage(caseForTab.caseId, caseForTab.lastIncomingMessageId);
            }, 5000);
        });
    }

    if (msg.type === "CASE_CONVO_LOADED") {
        // Forward it to the content script
        chrome.tabs.query({ active: true, currentWindow: true }, (tabs) => {
            chrome.tabs.sendMessage(tabs[0].id!, { type: "CASE_CONVO_LOADED", text: msg.text });
        });
    }
});

async function pollApiForLatestMessage(caseId: number, lastIncomingMessageId?: number) {
    try {
        console.log(`POLLING for last message, case: ${caseId} ${lastIncomingMessageId}`);
        const response = await fetch(`${apiBaseUrl}/vtiger/conversations/case/${caseId}/messages/latest`);
        if (!response.ok) throw new Error("Network response was not ok");

        const data = await response.json();
        console.log("API Response:", data);
        console.log(`Message id from API: ${data.data.id}, Last message seen: ${lastIncomingMessageId}`);
        if (data.data.id != lastIncomingMessageId) {
            console.log('Message id not the same, flash the tab')
            chrome.tabs.query({ active: true, currentWindow: true }, (tabs) => {
                chrome.tabs.sendMessage(tabs[0].id!, { type: "START_FLASHING_TAB" });
            });
        } else {
            console.log('Message id is the same, dont flash the tab');
        }
    } catch (err) {
        console.error("Polling error:", err);
    }
}