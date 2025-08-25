/// <reference types="chrome"/>

chrome.runtime.onInstalled.addListener(() => {
    console.log("Extension installed!");
});

const caseIdsByTab: Record<number, string> = {};

chrome.runtime.onMessage.addListener((msg, sender, sendResponse) => {

    if (msg.type === "CASE_OPENED") {
        const tabId = sender.tab?.id;
        if (!tabId) return;
        caseIdsByTab[tabId] = msg.caseId;
    }

    if (msg.type === "GET_LAST_CASE" && msg.tabId != null) {
        const tabId = msg.tabId;
        if (!tabId) return;
        sendResponse({ caseId: caseIdsByTab[tabId] || null });
    }
});