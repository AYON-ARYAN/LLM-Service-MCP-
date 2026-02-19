"use client";

export default function MessageBubble({ message }: any) {
  const isUser = message.startsWith("user::");
  const text = message.replace("user::", "").replace("ai::", "");

  return (
    <div className={`mb-4 flex ${isUser ? "justify-end" : "justify-start"}`}>
      <div
        className={`max-w-xl px-4 py-3 rounded-2xl text-sm ${
          isUser
            ? "bg-blue-600 text-white"
            : "bg-white/5 border border-white/10 text-gray-200"
        }`}
      >
        {text}
      </div>
    </div>
  );
}