console.log("Content script loaded!");

const div = document.createElement("div");
div.id = "vue-extension-root";
document.body.appendChild(div);

// Example: mount a Vue component dynamically
import { createApp } from "vue";
import HelloWorld from "./components/HelloWorld.vue";

createApp(HelloWorld).mount("#vue-extension-root");
