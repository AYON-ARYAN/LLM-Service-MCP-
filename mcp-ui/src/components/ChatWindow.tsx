"use client";

import { useState } from "react";
import MessageBubble from "./MessageBubble";
import { Send } from "lucide-react";

export default function ChatWindow({
  messages,
  setMessages,
  thinking,
  setThinking,
  setDocumentName,
}: any) {
  const [question, setQuestion] = useState("");

  const askAI = async () => {
    if (!question) return;

    setMessages((prev: string[]) => [...prev, "user::" + question]);
    setThinking("Thinking...");

    try {
      const res = await fetch("http://localhost:8084/ask", {
        method: "POST",
        headers: { "Content-Type": "text/plain" },
        body: question,
      });

      const data = await res.json();
      const answer =
        data.answer || data.choices?.[0]?.message?.content || "";

      setThinking(
        data.thoughts ||
        data.steps ||
        data.debug ||
        "Response generated"
      );

      setMessages((prev: string[]) => [...prev, "ai::" + answer]);
    } catch {
      setMessages((prev: string[]) => [...prev, "ai::Backend error"]);
      setThinking("Error");
    }

    setQuestion("");
  };

  const uploadFile = async (e: any) => {
    const file = e.target.files?.[0];
    if (!file) return;

    setDocumentName(file.name);

    const formData = new FormData();
    formData.append("file", file);

    await fetch("http://localhost:8084/docs/upload", {
      method: "POST",
      body: formData,
    });

    setThinking("Document indexed");
  };

  return (
    <div className="flex flex-col flex-1 p-8">
      <div className="flex-1 overflow-y-auto mb-4">
        {messages.map((msg: string, i: number) => (
          <MessageBubble key={i} message={msg} />
        ))}
      </div>

      <div className="flex gap-3">
        <input
  value={question}
  onChange={(e) => setQuestion(e.target.value)}
  onKeyDown={(e) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      askAI();
    }
  }}
  placeholder="Ask something intelligent..."
  className="flex-1 bg-[#111827] border border-white/10 rounded-xl px-4 py-3"
/>

        <label className="cursor-pointer bg-white/5 px-4 rounded-xl flex items-center">
          📎
          <input type="file" hidden onChange={uploadFile} />
        </label>

        <button
          onClick={askAI}
          className="bg-gradient-to-r from-cyan-500 to-blue-600 px-6 rounded-xl flex items-center"
        >
          <Send size={18} />
        </button>
      </div>
    </div>
  );
}