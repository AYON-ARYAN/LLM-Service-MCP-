"use client";

import { useState } from "react";
import Sidebar from "@/components/Sidebar";
import ChatWindow from "@/components/ChatWindow";
import StatusPanel from "@/components/StatusPanel";

export default function Home() {
  const [messages, setMessages] = useState<string[]>([]);
  const [thinking, setThinking] = useState("Idle");
  const [documentName, setDocumentName] = useState("");

  return (
    <main className="h-screen w-full bg-[#0b0f17] text-white flex">
      <Sidebar documentName={documentName} />
      <ChatWindow
        messages={messages}
        setMessages={setMessages}
        thinking={thinking}
        setThinking={setThinking}
        setDocumentName={setDocumentName}
      />
      <StatusPanel
        thinking={thinking}
        messages={messages}
        documentName={documentName}
      />
    </main>
  );
}