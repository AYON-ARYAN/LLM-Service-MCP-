"use client";

import { Sparkles } from "lucide-react";

export default function Sidebar({ documentName }: any) {
  return (
    <div className="w-60 bg-[#0e1422] border-r border-white/5 p-6 flex flex-col">
      <div className="flex items-center gap-2 mb-8">
        <Sparkles className="text-cyan-400" />
        <span className="font-bold text-lg">MCP AI</span>
      </div>

      <div className="text-xs text-gray-400 mb-2">Document</div>
      <div className="text-sm text-cyan-300">
        {documentName || "No document loaded"}
      </div>

      <div className="mt-auto text-xs text-gray-500">
        AI Core v1.0
      </div>
    </div>
  );
}