"use client";

import { useEffect, useMemo, useState } from "react";
import { api, ApiError } from "@/lib/api";
import { Alert, AlertClassification, EventDirection, Site, UnlockRequest } from "@/lib/types";

// No dedicated reporting endpoint exists on dadscare-backend yet — this view is a
// client-side aggregation over the same /api/v1/alerts and /api/v1/unlock-requests
// data the Alerts page uses, which is a real, honest report today. A server-side
// reports endpoint (date-range filters, pagination) is a natural next step once
// alert volume outgrows a single unpaginated fetch.

const CLASSIFICATIONS: AlertClassification[] = ["CONFIRMED", "UNEXPLAINED_HIGH", "UNEXPLAINED_VERIFY", "SUPPRESSED"];
const DIRECTIONS: EventDirection[] = ["OPEN", "CLOSE", "ALARM"];

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

const REPORT_HEADERS = ["Ref", "When", "Godown", "Device", "Direction", "Classification", "Score"];

function reportRows(alerts: Alert[]): string[][] {
  return alerts.map((a) => [
    a.sequenceCode ?? "",
    new Date(a.createdAt).toLocaleString(),
    a.siteName ?? "",
    a.deviceRef,
    a.direction,
    a.classification,
    a.confidenceScore != null ? String(a.confidenceScore) : "",
  ]);
}

async function downloadPdf(filename: string, alerts: Alert[]) {
  const [{ default: jsPDF }, autoTable] = await Promise.all([import("jspdf"), import("jspdf-autotable")]);
  const doc = new jsPDF({ orientation: "landscape" });
  doc.setFontSize(14);
  doc.text("Dad's Care — Alert Report", 14, 15);
  doc.setFontSize(9);
  doc.text(`Generated ${new Date().toLocaleString()} · ${alerts.length} alert(s)`, 14, 21);
  autoTable.default(doc, {
    startY: 26,
    head: [REPORT_HEADERS],
    body: reportRows(alerts),
    styles: { fontSize: 8 },
    headStyles: { fillColor: [198, 64, 42] }, // brand-red
  });
  doc.save(filename);
}

export default function ReportsPage() {
  const [alerts, setAlerts] = useState<Alert[] | null>(null);
  const [unlockRequests, setUnlockRequests] = useState<UnlockRequest[] | null>(null);
  const [sites, setSites] = useState<Site[]>([]);
  const [error, setError] = useState<string | null>(null);

  const [siteFilter, setSiteFilter] = useState("");
  const [deviceFilter, setDeviceFilter] = useState("");
  const [classificationFilter, setClassificationFilter] = useState<AlertClassification | "">("");
  const [directionFilter, setDirectionFilter] = useState<EventDirection | "">("");

  useEffect(() => {
    Promise.all([api.get<Alert[]>("/api/v1/alerts"), api.get<UnlockRequest[]>("/api/v1/unlock-requests")])
      .then(([a, u]) => {
        setAlerts(a);
        setUnlockRequests(u);
      })
      .catch((err) => setError(err instanceof ApiError ? err.message : "Failed to load report data."));
    api.get<Site[]>("/api/v1/sites").then(setSites).catch(() => {
      // Non-fatal — the godown filter dropdown just stays empty.
    });
  }, []);

  const deviceRefs = useMemo(() => {
    const set = new Set((alerts ?? []).map((a) => a.deviceRef));
    return Array.from(set).sort();
  }, [alerts]);

  const filteredAlerts = useMemo(() => {
    if (!alerts) return [];
    return alerts.filter((a) => {
      if (siteFilter && String(a.siteId) !== siteFilter) return false;
      if (deviceFilter && a.deviceRef !== deviceFilter) return false;
      if (classificationFilter && a.classification !== classificationFilter) return false;
      if (directionFilter && a.direction !== directionFilter) return false;
      return true;
    });
  }, [alerts, siteFilter, deviceFilter, classificationFilter, directionFilter]);

  const counts = useMemo(() => {
    const byClassification: Record<AlertClassification, number> = {
      CONFIRMED: 0,
      UNEXPLAINED_HIGH: 0,
      UNEXPLAINED_VERIFY: 0,
      SUPPRESSED: 0,
    };
    filteredAlerts.forEach((a) => {
      byClassification[a.classification] += 1;
    });
    const total = filteredAlerts.length;
    const confirmedRate = total > 0 ? Math.round((byClassification.CONFIRMED / total) * 100) : 0;
    return { byClassification, total, confirmedRate };
  }, [filteredAlerts]);

  const filteredDeviceIds = useMemo(() => new Set(filteredAlerts.map((a) => a.deviceId)), [filteredAlerts]);
  // "Relayed" = successfully handed off to Velosyss at all (QUEUED/DISPATCHED/RESPONDED/EXPIRED all
  // reached Velosyss — only PENDING (not yet sent), FAILED (our-side send error), and
  // DEVICE_OFFLINE (Velosyss rejected outright, per §6.2 of the Integration Guide) don't count).
  const relayedCount = useMemo(
    () =>
      (unlockRequests ?? []).filter(
        (u) =>
          !["PENDING", "FAILED", "DEVICE_OFFLINE"].includes(u.status) && filteredDeviceIds.has(u.deviceId),
      ).length,
    [unlockRequests, filteredDeviceIds],
  );

  const hasActiveFilters = siteFilter || deviceFilter || classificationFilter || directionFilter;

  return (
    <div>
      <h1 className="text-2xl font-bold text-gray-800 mb-1">Reports</h1>
      <p className="text-gray-500 mb-6">A summary view over your alert and unlock-request history.</p>

      {error && <p className="text-red-600 mb-4">{error}</p>}
      {alerts === null && !error && <p className="text-gray-500">Loading…</p>}

      {alerts && (
        <>
          <div className="bg-white rounded-lg shadow p-4 mb-6 flex flex-wrap gap-3">
            <select
              value={siteFilter}
              onChange={(e) => setSiteFilter(e.target.value)}
              className="px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-brand-red"
            >
              <option value="">All godowns</option>
              {sites.map((s) => (
                <option key={s.id} value={s.id}>
                  {s.name}
                </option>
              ))}
            </select>
            <select
              value={deviceFilter}
              onChange={(e) => setDeviceFilter(e.target.value)}
              className="px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-brand-red"
            >
              <option value="">All devices</option>
              {deviceRefs.map((ref) => (
                <option key={ref} value={ref}>
                  {ref}
                </option>
              ))}
            </select>
            <select
              value={classificationFilter}
              onChange={(e) => setClassificationFilter(e.target.value as AlertClassification | "")}
              className="px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-brand-red"
            >
              <option value="">All statuses</option>
              {CLASSIFICATIONS.map((c) => (
                <option key={c} value={c}>
                  {c}
                </option>
              ))}
            </select>
            <select
              value={directionFilter}
              onChange={(e) => setDirectionFilter(e.target.value as EventDirection | "")}
              className="px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-brand-red"
            >
              <option value="">Open + Close</option>
              {DIRECTIONS.map((d) => (
                <option key={d} value={d}>
                  {d}
                </option>
              ))}
            </select>
            {hasActiveFilters && (
              <button
                onClick={() => {
                  setSiteFilter("");
                  setDeviceFilter("");
                  setClassificationFilter("");
                  setDirectionFilter("");
                }}
                className="text-sm text-gray-500 hover:text-brand-red"
              >
                Clear filters
              </button>
            )}
          </div>

          <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-8">
            <StatCard label="Total alerts" value={counts.total} />
            <StatCard
              label="Confirmed shutter opens"
              value={counts.byClassification.CONFIRMED}
              sub={`${counts.confirmedRate}% of total`}
            />
            <StatCard label="Unexplained (high)" value={counts.byClassification.UNEXPLAINED_HIGH} />
            <StatCard label="Unlock requests relayed" value={relayedCount} />
          </div>

          <div className="flex flex-wrap items-center justify-between gap-3 mb-3">
            <h2 className="text-lg font-semibold text-gray-800">Alert history</h2>
            <div className="flex gap-2">
              <button
                onClick={() => downloadCsv("dadscare-alerts.csv", [REPORT_HEADERS, ...reportRows(filteredAlerts)])}
                className="text-sm px-4 py-2 rounded-lg bg-brand-red hover:bg-brand-red-dark text-white font-medium"
              >
                Export CSV
              </button>
              <button
                onClick={() => downloadPdf("dadscare-alerts.pdf", filteredAlerts)}
                className="text-sm px-4 py-2 rounded-lg bg-brand-green hover:bg-brand-green-dark text-white font-medium"
              >
                Export PDF
              </button>
            </div>
          </div>

          <div className="bg-white rounded-lg shadow overflow-x-auto">
            <table className="w-full text-left">
              <thead className="bg-gray-50 text-xs uppercase text-gray-500">
                <tr>
                  <th className="px-4 py-3">Ref</th>
                  <th className="px-4 py-3">When</th>
                  <th className="px-4 py-3">Godown</th>
                  <th className="px-4 py-3">Device</th>
                  <th className="px-4 py-3">Direction</th>
                  <th className="px-4 py-3">Classification</th>
                </tr>
              </thead>
              <tbody>
                {filteredAlerts.map((a) => (
                  <tr key={a.id} className="border-t border-gray-100">
                    <td className="px-4 py-3 font-mono text-sm text-gray-700">{a.sequenceCode ?? "—"}</td>
                    <td className="px-4 py-3 text-sm text-gray-500">{new Date(a.createdAt).toLocaleString()}</td>
                    <td className="px-4 py-3 text-sm text-gray-700">{a.siteName ?? "—"}</td>
                    <td className="px-4 py-3 text-sm text-gray-700">{a.deviceRef}</td>
                    <td className="px-4 py-3 text-sm text-gray-700">{a.direction}</td>
                    <td className="px-4 py-3 text-sm text-gray-700">{a.classification}</td>
                  </tr>
                ))}
                {filteredAlerts.length === 0 && (
                  <tr>
                    <td colSpan={6} className="px-4 py-6 text-center text-gray-500">
                      {alerts.length === 0 ? "No alerts yet." : "No alerts match your filters."}
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
