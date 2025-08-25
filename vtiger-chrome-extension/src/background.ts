/// <reference types="chrome"/>

chrome.runtime.onInstalled.addListener(() => {
    console.log("Extension installed!");
});

let lastCaseId: string | null = null;

chrome.runtime.onMessage.addListener((msg, sender, sendResponse) => {
  if (msg.type === "CASE_OPENED") {
    lastCaseId = msg.caseId;
  }
  if (msg.type === "GET_LAST_CASE") {
    console.log('GET LAST CASE')
    sendResponse({ caseId: lastCaseId });
  }
});