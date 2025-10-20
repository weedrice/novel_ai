"use client";

import { useEffect, useState } from "react";

type Check = {
  name: string;
  url?: string;
  status: "ok" | "fail" | "pending";
  detail?: string;
};

export default function StatusPage() {
  const [checks, setChecks] = useState<Check[]>([
    { name: "Frontend", url: "/api/health", status: "pending" },
    {
      name: "API Server",
      url: `${process.env.NEXT_PUBLIC_API_BASE || "http://localhost:8080"}/health`,
      status: "pending",
    },
  ]);

  useEffect(() => {
    async function run() {
      const updated = await Promise.all(
        checks.map(async (c) => {
          try {
            const res = await fetch(c.url!, { cache: "no-store" });
            if (!res.ok) return { ...c, status: "fail", detail: `HTTP ${res.status}` };
            const j = await res.json().catch(() => ({}));
            return { ...c, status: "ok", detail: JSON.stringify(j) };
          } catch (e: any) {
            return { ...c, status: "fail", detail: e?.message || "error" };
          }
        })
      );
      setChecks(updated);
    }
    run();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <div style={{ padding: 24 }}>
      <h1>Service Status</h1>
      <p>Checks for Frontend and API (LLM via API later).</p>
      <div style={{ marginTop: 16 }}>
        {checks.map((c) => (
          <div
            key={c.name}
            style={{
              display: "flex",
              alignItems: "center",
              gap: 12,
              padding: "8px 0",
              borderBottom: "1px solid #eee",
            }}
          >
            <strong style={{ width: 140 }}>{c.name}</strong>
            <span
              style={{
                padding: "2px 8px",
                borderRadius: 8,
                background: c.status === "ok" ? "#e6ffed" : c.status === "pending" ? "#fffbe6" : "#ffebe6",
                color: c.status === "ok" ? "#067d36" : c.status === "pending" ? "#8a6d3b" : "#a8071a",
              }}
            >
              {c.status.toUpperCase()}
            </span>
            <code style={{ fontSize: 12, color: "#666" }}>{c.url}</code>
            <span style={{ fontSize: 12, color: "#999" }}>{c.detail}</span>
          </div>
        ))}
      </div>
    </div>
  );
}

