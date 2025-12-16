async function sendMessage() {
    const input = document.getElementById("question");
    const question = input.value.trim();
    if (!question) return;

    addMessage(question, "user");
    input.value = "";

    const botMsg = addMessage("⏳ L'assistant rédige...", "bot", true);

    const response = await fetch("/api/chat", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({question, k: 3})
    });

    const data = await response.json();

    botMsg.classList.remove("typing");
    botMsg.innerHTML = "";

    typeWriterMarkdown(botMsg, removeEnglishLines(data.answer));
    removeWelcome();
}

function typeWriterMarkdown(element, text, speed = 15) {
  let i = 0;
  let buffer = "";

  const interval = setInterval(() => {
    buffer += text.charAt(i);
    element.innerHTML = marked.parse(buffer);
    element.scrollIntoView({ behavior: "smooth", block: "end" });
    i++;

    if (i >= text.length) clearInterval(interval);
  }, speed);
}


function addMessage(text, type, typing = false) {
    const chatBox = document.getElementById("chat-box");
    const msg = document.createElement("div");
    msg.className = `message ${type}`;
    if (typing) msg.classList.add("typing");
    msg.textContent = text;
    chatBox.appendChild(msg);
    chatBox.scrollTop = chatBox.scrollHeight;
    return msg;
}

function handleEnter(e) {
    if (e.key === "Enter") {
        sendMessage();
    }
}

function removeEnglishLines(text) {
  return text
    .split("\n")
    .filter(line => !/^[A-Za-z ,.'-]+$/.test(line))
    .join("\n");
}


function removeWelcome() {
  const welcome = document.querySelector(".welcome");
  if (welcome) welcome.remove();
}



