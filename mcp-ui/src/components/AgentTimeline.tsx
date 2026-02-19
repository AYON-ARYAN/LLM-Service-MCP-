"use client";

import { motion } from "framer-motion";

export default function AgentTimeline({ thinking }: any) {
  if (!thinking?.includes("STEP")) {
    return <div className="text-gray-400">{thinking}</div>;
  }

  const parts = thinking.split(/--- STEP (\d+) ---/g);
  const steps = [];

  for (let i = 1; i < parts.length; i += 2) {
    const stepNum = parts[i];
    const content = parts[i + 1];

    const tool = content.match(/TOOL: ([^\n]*)/)?.[1] || "—";
    const thought = content.match(/THOUGHT: ([\s\S]*?)QUERY:/)?.[1] || "";
    const query = content.match(/QUERY: ([\s\S]*?)ACTION:/)?.[1] || "";
    const action = content.match(/ACTION: ([^\n]*)/)?.[1] || "";

    steps.push(
      <motion.div
        key={i}
        initial={{ opacity: 0, y: 10 }}
        animate={{ opacity: 1, y: 0 }}
        className="bg-[#0f172a] border border-cyan-500/20 rounded-xl p-4 mb-4"
      >
        <div className="text-xs text-cyan-400 mb-2">
          STEP {stepNum} • {tool}
        </div>
        <div className="text-sm text-gray-200 mb-2">{thought}</div>
        <div className="text-sm text-blue-300 mb-2">{query}</div>
        <div className="text-sm text-purple-300">{action}</div>
      </motion.div>
    );
  }

  return <div className="overflow-y-auto">{steps.reverse()}</div>;
}