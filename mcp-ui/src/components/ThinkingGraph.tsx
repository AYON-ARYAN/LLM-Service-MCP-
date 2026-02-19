"use client";

export default function ThinkingGraph({ thinking }: any) {
  const steps = thinking?.match(/STEP \d+/g) || [];

  return (
    <div className="bg-black/40 rounded-xl p-4 border border-white/5">
      <div className="text-xs text-cyan-400 mb-4">Agent Graph View</div>

      <div className="flex gap-4 overflow-x-auto">
        {steps.map((s: string, i: number) => (
          <div
            key={i}
            className="min-w-[120px] bg-gradient-to-br from-cyan-500/20 to-blue-500/20 border border-cyan-500/20 rounded-lg p-3 text-center"
          >
            {s}
          </div>
        ))}
      </div>
    </div>
  );
}