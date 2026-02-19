"use client";

import AgentTimeline from "./AgentTimeline";
import ThinkingGraph from "./ThinkingGraph";

export default function StatusPanel({
  thinking,
  messages,
  documentName,
}: any) {
  return (
    <div className="w-1/3 border-l border-white/5 bg-[#0d1320] p-6 flex flex-col">

      <div className="text-lg font-bold mb-4">AI Status</div>

      <div className="grid grid-cols-2 gap-3 mb-6">
        <Metric
          title="Questions"
          value={messages.filter((m: string) => m.startsWith("user::")).length}
        />
        <Metric
          title="Doc Loaded"
          value={documentName ? "Yes" : "No"}
        />
      </div>

      <ThinkingGraph thinking={thinking} />

      <div className="mt-6 flex-1 overflow-y-auto">
        <AgentTimeline thinking={thinking} />
      </div>
    </div>
  );
}

function Metric({ title, value }: any) {
  return (
    <div className="bg-white/5 border border-white/10 rounded-xl p-3">
      <div className="text-xs text-gray-400">{title}</div>
      <div className="text-lg font-bold">{value}</div>
    </div>
  );
}