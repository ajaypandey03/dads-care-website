"use client";

import { useEffect, useMemo, useState } from "react";
import { api, ApiError } from "@/lib/api";
import { Alert, AlertClassification, UnlockRequest } from "@/lib/types";

// No dedicated reporting endpoint exists on dadscare-backend yet — this view is a
// client-side aggregation over the same /api/v1/alerts and /api/v1/unlock-requests
// data the Alerts page uses, which is a real, honest report today. A server-side
// reports endpoint (date-range filters, pagination) is a natural next step once
// alert volume outgrows a single unpaginated fetch.

function StatCard({ label, value, sub }: { label: string; value: string | number; sub?: string }) {
  return (
    <div className="bg-white rounded-lg shadow p-5">
      <p className="text-sm text-gray-500">{label}</p>
      <p className="text-3xl font-bold text-gray-800 mt-1">{value}</p>
      {sub && <p className="text-xs text-gray-400 mt-1">{sub}</p>}
    </div>
  );
}

function downloadCsv(filename: string, rows: string[][]) {
  const csv = rows.map((row) => row.map((cell) => `"${(cell ?? "").replace(/"/g, '""')}"`).join(",")).join("\n");
  const blob = new Blob([csv], { type: "text/csv;charset=utf-8;" });
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.download = filename;
  link.click();
  URL.revokeObjectURL(url);
}

export default function ReportsPage() {
  const [alerts, setAlerts] = useState<Alert[] | null>(null);
  const [unlockRequests, setUnlockRequests] = useState<UnlockRequest[] | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    Promise.all([api.get<Alert[]>("/api/v1/alerts"), api.get<UnlockRequest[]>("/api/v1/unlock-requests")])
      .then(([a, u]) => {
        setAlerts(a);
        setUnlockRequests(u);
      })
      .catch((err) => setError(err instanceof ApiError ? err.message : "Failed to load report data."));
  }, []);

  const counts = useMemo(() => {
    const byClassification: Record<AlertClassification, number> = {
      CONFIRMED: 0,
      UNEXPLAINED_HIGH: 0,
      UNEXPLAINED_VERIFY: 0,
      SUPPRESSED: 0,
    };
    (alerts ?? []).forEach((a) => {
      byClassification[a.classification] += 1;
    });
    const total = alerts?.length ?? 0;
    const confirmedRate = total > 0 ? Math.round((byClassification.CONFIRMED / total) * 100) : 0;
    return { byClassification, total, confirmedRate };
  }, [alerts]);

  const exportAlertsCsv = () => {
    if (!alerts) return;
    downloadCsv("dadscare-alerts.csv", [
      ["Ref", "When", "Device ID", "Direction", "Classification", "Confidence Score"],
      ...alerts.map((a) => [
        a.sequenceCode ?? "",
        new Date(a.createdAt).toISOString(),
        String(a.deviceId),
        a.direction,
        a.classification,
        a.confidenceScore != null ? String(a.confidenceScore) : "",
      ]),
    ]);
  };

  return (
    <div>
      <h1 className="text-2xl font-bold text-gray-800 mb-1">Reports</h1>
      <p className="text-gray-500 mb-6">A summary view over your alert and unlock-request history.</p>

      {error && <p className="text-red-600 mb-4">{error}</p>}
      {alerts === null && !error && <p className="text-gray-500">Loading…</p>}

      {alerts && (
        <>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-8">
            <StatCard label="Total alerts" value={counts.total} />
            <StatCard label="Confirmed shutter opens" value={counts.byClassification.CONFIRMED} sub={`${counts.confirmedRate}% of total`} />
            <StatCard label="Unexplained (high)" value={counts.byClassification.UNEXPLAINED_HIGH} />
            <StatCard label="Unlock requests relayed" value={unlockRequests?.filter((u) => u.status === "RELAYED").length ?? "—"} />
          </div>

          <div className="flex items-center justify-between mb-3">
            <h2 className="text-lg font-semibold text-gray-800">Alert history</h2>
            <button
              onClick={exportAlertsCsv}
              className="text-sm px-4 py-2 rounded-lg bg-brand-red hover:bg-brand-red-dark text-white font-medium"
            >
              Export CSV
            </button>
          </div>

          <div className="bg-white rounded-lg shadow overflow-x-auto">
            <table className="w-full text-left">
              <thead className="bg-gray-50 text-xs uppercase text-gray-500">
                <tr>
                  <th className="px-4 py-3">Ref</th>
                  <th className="px-4 py-3">When</th>
                  <th className="px-4 py-3">Direction</th>
                  <th className="px-4 py-3">Classification</th>
                </tr>
              </thead>
              <tbody>
                {alerts.map((a) => (
                  <tr key={a.id} className="border-t border-gray-100">
                    <td className="px-4 py-3 font-mono text-sm text-gray-700">{a.sequenceCode ?? "—"}</td>
                    <td className="px-4 py-3 text-sm text-gray-500">{new Date(a.createdAt).toLocaleString()}</td>
                    <td className="px-4 py-3 text-sm text-gray-700">{a.direction}</td>
                    <td className="px-4 py-3 text-sm text-gray-700">{a.classification}</td>
                  </tr>
                ))}
                {alerts.length === 0 && (
                  <tr>
                    <td colSpan={4} className="px-4 py-6 text-center text-gray-500">
                      No alerts yet.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </>
      )}
    </div>
  );
}
