"use client";

import { useEffect, useMemo, useState } from "react";
import { api, ApiError } from "@/lib/api";
import { Site, ShutterUnit, ShutterState } from "@/lib/types";

function OnlineBadge({ online }: { online: boolean }) {
  return (
    <span
      className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold ${
        online ? "bg-green-100 text-green-700" : "bg-gray-200 text-gray-600"
      }`}
    >
      <span className={`w-1.5 h-1.5 rounded-full ${online ? "bg-green-500" : "bg-gray-400"}`} />
      {online ? "Online" : "Offline"}
    </span>
  );
}

const STATE_STYLE: Record<ShutterState, string> = {
  OPEN: "bg-red-100 text-red-700",
  CLOSED: "bg-green-100 text-green-700",
  UNKNOWN: "bg-gray-100 text-gray-500",
};

const STATE_LABEL: Record<ShutterState, string> = {
  OPEN: "Open",
  CLOSED: "Closed",
  UNKNOWN: "Unknown",
};

function StateBadge({ state }: { state: ShutterState }) {
  return (
    <span className={`inline-flex items-center px-2.5 py-1 rounded-full text-xs font-semibold ${STATE_STYLE[state]}`}>
      {STATE_LABEL[state]}
    </span>
  );
}

function formatTimestamp(value: string | null): string {
  if (!value) return "Never";
  return new Date(value).toLocaleString();
}

function ShutterRow({ unit }: { unit: ShutterUnit }) {
  return (
    <div className="py-3 px-4 border-t border-gray-100">
      <div className="flex items-center justify-between gap-3">
        <div>
          <p className="font-medium text-gray-800">{unit.label}</p>
          <p className="text-sm text-gray-500">
            {unit.device ? unit.device.velosyssDeviceRef : "No lock device linked"}
            {unit.device?.lastBatteryPct != null && ` · Battery ${unit.device.lastBatteryPct}%`}
          </p>
        </div>
        <div className="flex items-center gap-2 shrink-0">
          <StateBadge state={unit.currentState} />
          {unit.device && <OnlineBadge online={unit.device.online} />}
        </div>
      </div>
      <div className="mt-2 grid grid-cols-2 gap-x-4 text-xs text-gray-400">
        <span>Last opened: {formatTimestamp(unit.lastOpenedAt)}</span>
        <span>Last closed: {formatTimestamp(unit.lastClosedAt)}</span>
      </div>
    </div>
  );
}

function statusSummary(units: ShutterUnit[] | null): string {
  if (!units) return "Loading shutters…";
  if (units.length === 0) return "No shutter units configured";
  const open = units.filter((u) => u.currentState === "OPEN").length;
  const closed = units.filter((u) => u.currentState === "CLOSED").length;
  const unknown = units.length - open - closed;
  const parts = [];
  if (open > 0) parts.push(`${open} open`);
  if (closed > 0) parts.push(`${closed} closed`);
  if (unknown > 0) parts.push(`${unknown} unknown`);
  return `${units.length} shutter${units.length === 1 ? "" : "s"} — ${parts.join(", ")}`;
}

function SiteCard({ site, units, unitsError }: { site: Site; units: ShutterUnit[] | null; unitsError: string | null }) {
  const [expanded, setExpanded] = useState(false);
  const anyOpen = units?.some((u) => u.currentState === "OPEN") ?? false;

  return (
    <div className="bg-white rounded-lg shadow overflow-hidden">
      <button
        onClick={() => setExpanded((v) => !v)}
        className="w-full flex items-center justify-between px-4 py-4 text-left hover:bg-gray-50 transition-colors"
      >
        <div>
          <div className="flex items-center gap-2">
            <p className="font-semibold text-gray-800">{site.name}</p>
            {anyOpen && <span className="w-2 h-2 rounded-full bg-red-500" title="At least one shutter open" />}
          </div>
          <p className="text-sm text-gray-500">
            {site.godownCode}
            {site.address && ` · ${site.address}`}
          </p>
          <p className="text-xs text-gray-400 mt-1">
            {unitsError ? <span className="text-red-500">{unitsError}</span> : statusSummary(units)}
          </p>
        </div>
        <span className="text-gray-400 text-sm shrink-0">{expanded ? "Hide details ▲" : "View details ▼"}</span>
      </button>
      {expanded && (
        <div className="bg-gray-50">
          {units && units.length === 0 && (
            <p className="px-4 py-3 text-sm text-gray-500">No shutter units configured for this site.</p>
          )}
          {units?.map((unit) => (
            <ShutterRow key={unit.id} unit={unit} />
          ))}
        </div>
      )}
    </div>
  );
}

export default function DashboardOverviewPage() {
  const [sites, setSites] = useState<Site[] | null>(null);
  const [unitsBySite, setUnitsBySite] = useState<Record<number, ShutterUnit[]>>({});
  const [unitErrorsBySite, setUnitErrorsBySite] = useState<Record<number, string>>({});
  const [error, setError] = useState<string | null>(null);
  const [search, setSearch] = useState("");

  useEffect(() => {
    api
      .get<Site[]>("/api/v1/sites")
      .then(async (data) => {
        setSites(data);
        // Fetched eagerly (not on-expand) so the collapsed card can show a live open/closed
        // summary without the customer needing to open every site first.
        await Promise.all(
          data.map((site) =>
            api
              .get<ShutterUnit[]>(`/api/v1/sites/${site.id}/shutter-units`)
              .then((units) => setUnitsBySite((prev) => ({ ...prev, [site.id]: units })))
              .catch((err) =>
                setUnitErrorsBySite((prev) => ({
                  ...prev,
                  [site.id]: err instanceof ApiError ? err.message : "Failed to load shutters.",
                })),
              ),
          ),
        );
      })
      .catch((err) => setError(err instanceof ApiError ? err.message : "Failed to load sites."));
  }, []);

  const filteredSites = useMemo(() => {
    if (!sites) return null;
    const q = search.trim().toLowerCase();
    if (!q) return sites;
    return sites.filter(
      (s) =>
        s.name.toLowerCase().includes(q) ||
        s.godownCode.toLowerCase().includes(q) ||
        (s.address ?? "").toLowerCase().includes(q),
    );
  }, [sites, search]);

  return (
    <div>
      <h1 className="text-2xl font-bold text-gray-800 mb-1">Godown Status</h1>
      <p className="text-gray-500 mb-6">Live shutter/lock status across every site in your organization.</p>

      {sites && sites.length > 0 && (
        <input
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder="Search by godown name, code, or address…"
          className="w-full max-w-md mb-6 px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-brand-red"
        />
      )}

      {error && <p className="text-red-600 mb-4">{error}</p>}
      {sites === null && !error && <p className="text-gray-500">Loading sites…</p>}
      {sites?.length === 0 && <p className="text-gray-500">No sites configured yet.</p>}
      {sites && sites.length > 0 && filteredSites?.length === 0 && (
        <p className="text-gray-500">No godowns match &quot;{search}&quot;.</p>
      )}

      <div className="space-y-3">
        {filteredSites?.map((site) => (
          <SiteCard
            key={site.id}
            site={site}
            units={unitsBySite[site.id] ?? null}
            unitsError={unitErrorsBySite[site.id] ?? null}
          />
        ))}
      </div>
    </div>
  );
}
