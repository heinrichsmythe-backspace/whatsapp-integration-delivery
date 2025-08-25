console.log("Content script loaded!");

function watchUrlForCase() {
    function checkUrl() {
        console.log('checkUrl ' + window.location.search)
        const params = new URLSearchParams(window.location.search);
        if (params.get("module") === "Cases") {
            const id = params.get("id");
            console.log("Case detected via URL:", id);
            console.log('sending CASE_OPENED & id to vue app');
            chrome.runtime.sendMessage({ type: "CASE_OPENED", caseId: id });
        }
    }

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

function watchForCasesDiv() {
    console.log("Watching for open case modals...");

    const observer = new MutationObserver(() => {
        // Select all modals that are currently visible
        const modals = document.querySelectorAll('div[id^="detailRecordPreview_"].show');
        modals.forEach(modal => {
            const id = modal.id.split("_")[1]; // extract the numeric ID
            console.log("Case modal open with ID:", id);
            console.log('sending CASE_OPENED & id to vue app');
            chrome.runtime.sendMessage({ type: "CASE_OPENED", caseId: id });
        });
    });

    observer.observe(document.body, { childList: true, subtree: true });
}

// Run on content script load
if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", watchForCasesDiv);
} else {
    watchForCasesDiv();
    watchUrlForCase();
}
