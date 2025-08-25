console.log("Content script loaded!");

function watchUrlForCase() {
    // Check once on load
    checkUrl();

    // Listen for SPA navigation (history pushState / replaceState)
    const pushState = history.pushState;
    history.pushState = function (...args) {
        pushState.apply(this, args);
        checkUrl();
    };

    const replaceState = history.replaceState;
    history.replaceState = function (...args) {
        replaceState.apply(this, args);
        checkUrl();
    };

    // Also detect normal hash changes
    window.addEventListener("popstate", checkUrl);
}

function watchForCasesModal() {
    console.log("Watching for open case modals...");

    const observer = new MutationObserver(() => {
        // Select all modals that are currently visible
        lookForCaseModal();
    });

    observer.observe(document.body, { childList: true, subtree: true });
}

const checkUrl = () => {
    console.log('checkUrl ' + window.location.search)
    const params = new URLSearchParams(window.location.search);
    if (params.get("module") === "Cases") {
        const id = params.get("id");
        console.log("Case detected via URL:", id);
        if(id){
            console.log('sending CASE_OPENED & id to vue app');
            chrome.runtime.sendMessage({ type: "CASE_OPENED", caseId: id });
        }
    }
}

const lookForCaseModal = () => {
    const modals = document.querySelectorAll('div[id^="detailRecordPreview_"].show');
    modals.forEach(modal => {
        const id = modal.id.split("_")[1]; // extract the numeric ID
        console.log("Case modal open with ID:", id);
        if(id){
            console.log('sending CASE_OPENED & id to vue app');
            chrome.runtime.sendMessage({ type: "CASE_OPENED", caseId: id });
        }
    });
}

// Run on content script load
if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", watchForCasesModal);
} else {
    watchForCasesModal();
    watchUrlForCase();
}

chrome.runtime.onMessage.addListener((message, sender, sendResponse) => {
    if (message.type === "RECHECK_FOR_CASE_IN_CONTENT") {
        console.log('check')
        lookForCaseModal();
        watchUrlForCase();
    }
});

let flashing = false;
let originalTitle = document.title;
let flashInterval: number;

function startFlashing() {
  if (flashing) return;
  flashing = true;
  flashInterval = setInterval(() => {
    document.title =
      document.title === "🔔 New Message!" ? originalTitle : "🔔 New Message!";
  }, 1000);
}

function stopFlashing() {
  flashing = false;
  clearInterval(flashInterval);
  document.title = originalTitle;
}