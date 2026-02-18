"use client";

import { useState } from "react";
import { motion } from "framer-motion";
import { Send } from "lucide-react";

export default function Home() {

  const [question, setQuestion] = useState("");
  const [messages, setMessages] = useState<string[]>([]);
  const [thinking, setThinking] = useState("Idle");
  const [response, setResponse] = useState("");
  const [thoughts, setThoughts] = useState("");
  const [documentName, setDocumentName] = useState("");

  const typeText = (text: string) => {
    let i = 0;
    setResponse("");
    let interval = setInterval(() => {
      setResponse(prev => prev + text.charAt(i));
      i++;
      if (i >= text.length) clearInterval(interval);
    }, 10);
  };

  const uploadFile = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    setDocumentName(file.name);
    const formData = new FormData();
    formData.append("file", file);

    setThinking("Reading document...");

    await fetch("http://localhost:8084/docs/upload", {
      method: "POST",
      body: formData,
    });

    setThinking("Document indexed.");
  };

  const askAI = async () => {

    if (!question) return;

    setMessages(prev => [...prev, "🧑 " + question]);
    setThinking("Thinking...");

    try {
      const res = await fetch("http://localhost:8084/ask", {
        method: "POST",
        headers: { "Content-Type": "text/plain" },
        body: question
      });

      const data = await res.json();

      // Extract the answer from the response structure
      let answer = data.answer;
      
      // If the response is a full API response (like from Groq/OpenAI), extract the message content
      if (data.choices && data.choices.length > 0) {
        answer = data.choices[0].message.content;
      }
      
      // If there's thoughts data, show it
      if (data.thoughts) {
        setThinking(data.thoughts);
        setThoughts(data.thoughts);
      } else {
        setThinking("Complete");
      }

      // Update any DOM elements if present (non-React fallback for quick debugging)
      const responseEl = document.getElementById("responseText") as HTMLDivElement | null;
      if (responseEl && data.finalAnswer) {
        responseEl.innerText = data.finalAnswer;
      }

      const thoughtsEl = document.getElementById("thoughts") as HTMLDivElement | null;
      if (thoughtsEl) {
        thoughtsEl.innerHTML = `
<b>🧠 Intent:</b> ${data.intent || ""}<br>
<b>⚙ Tool:</b> ${data.tool || ""}<br>
<b>📊 Reason:</b> ${data.reasoning || ""}<br>
<b>📄 Context:</b> ${data.context || ""}
`;
      }

      let clean = answer
        .replace(/\\n/g, "\n")
        .replace(/\*\*/g, "")
        .replace(/\[/g, "")
        .replace(/\]/g, "");

      typeText(clean);
      setMessages(prev => [...prev, "🤖 " + clean]);

    } catch (err) {
      setMessages(prev => [...prev, "❌ Backend error"]);
      setThinking("Error");
    }

    setQuestion("");
  };

  const handleKeyPress = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      askAI();
    }
  };

  return (
    <main className="h-screen w-full text-white flex">

      {/* LEFT PANEL - CHAT */}
      <div className="flex flex-col w-2/3 p-8">

        <div className="topbar">
          <h1 className="float">
            MCP AI Core
          </h1>
        </div>

        {documentName && (
          <motion.div
            initial={{ opacity: 0, y: -10 }}
            animate={{ opacity: 1, y: 0 }}
            className="mb-4 px-4 py-3 glass-panel text-sm"
          >
            <span className="text-gray-300">📎 Document: </span>
            <span className="text-blue-300 font-medium">{documentName}</span>
          </motion.div>
        )}

        {/* CHAT WINDOW */}
        <div className="flex-1 glass-panel mb-4 overflow-y-auto">

          {messages.map((msg, i) => (
            <motion.div
              key={i}
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.3 }}
              className="message-container mb-4"
            >
              <div
                className={`p-4 rounded-2xl backdrop-blur-lg ${
                  msg.startsWith("🧑")
                    ? "bg-blue-500/10 border border-blue-500/20"
                    : "bg-transparent border border-transparent"
                }`}
              >
                {msg.startsWith("🤖") ? (
                  <div className="ai-response">
                    {msg.replace(/\\n/g, "\n").split("\n").map((line, idx) => (
                      <p key={idx}>{line}</p>
                    ))}
                  </div>
                ) : (
                  <div className="text-gray-100">
                    {msg.replace(/\\n/g, "\n")}
                  </div>
                )}
              </div>
            </motion.div>
          ))}

          {thoughts && (
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.3 }}
              className="thought-box"
            >
              <h3>🧠 AI Reasoning</h3>
              <p>{thoughts}</p>
            </motion.div>
          )}

        </div>

        {/* INPUT AREA */}
        <div className="flex gap-3 items-center">

          <input
            value={question}
            onChange={(e) => setQuestion(e.target.value)}
            onKeyPress={handleKeyPress}
            placeholder="Ask anything from document..."
            className="glass-input flex-1 px-4 py-3 placeholder-gray-400"
          />

          <label className="upload-btn">
            📄
            <input type="file" hidden onChange={uploadFile}/>
          </label>

          <button
            onClick={askAI}
            className="glass-button px-6 py-3 flex items-center gap-2"
          >
            <Send size={18} />
            Ask
          </button>

        </div>
      </div>

      {/* RIGHT PANEL - AI THINKING */}
      <div className="w-1/3 p-8 border-l border-white/10 flex flex-col">

        <h2 className="text-2xl font-bold mb-6 text-white">AI Thinking</h2>

        <motion.div
          key={thinking}
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ duration: 0.3 }}
          className="glass-panel flex-1 p-6 flex flex-col"
        >
          <div className="flex items-center gap-3 mb-4">
            <div className="status-pulse"></div>
            <span className="text-blue-300 font-semibold">AI Status</span>
          </div>

          <pre className="text-sm text-gray-300 whitespace-pre-wrap font-mono flex-1 overflow-y-auto">
{thinking}
</pre>

          <div id="responseText" className="mt-4 mb-2 text-sm text-white font-medium" />

          <div id="thoughts" className="mb-4 text-sm text-gray-200" />

          <div className="mt-4 text-xs text-gray-400 pt-4 border-t border-white/10">
            Reasoning engine active • Memory active • RAG active
          </div>
        </motion.div>

      </div>
    </main>
  );
}