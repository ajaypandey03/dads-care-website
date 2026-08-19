"use client";

import { useEffect, useState } from "react";
import { api, ApiError } from "@/lib/api";
import { Site, ShutterUnit } from "@/lib/types";

function StatusBadge({ online }: { online: boolean }) {
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

function ShutterRow({ unit }: { unit: ShutterUnit }) {
  return (
    <div className="flex items-center justify-between py-3 px-4 border-t border-gray-100">
      <div>
        <p className="font-medium text-gray-800">{unit.label}</p>
        <p className="text-sm text-gray-500">
          {unit.device ? unit.device.velosyssDeviceRef : "No lock device linked"}
          {unit.device?.lastBatteryPct != null && ` · Battery ${unit.device.lastBatteryPct}%`}
        </p>
      </div>
      <div className="flex items-center gap-3">
        <span className="text-sm text-gray-500">{unit.status}</span>
        {unit.device && <StatusBadge online={unit.device.online} />}
      </div>
    </div>
  );
}

function SiteCard({ site }: { site: Site }) {
  const [expanded, setExpanded] = useState(false);
  const [units, setUnits] = useState<ShutterUnit[] | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const toggle = async () => {
    const next = !expanded;
    setExpanded(next);
    if (next && units === null) {
      setLoading(true);
      setError(null);
      try {
        const data = await api.get<ShutterUnit[]>(`/api/v1/sites/${site.id}/shutter-units`);
        setUnits(data);
      } catch (err) {
        setError(err instanceof ApiError ? err.message : "Failed to load shutter units.");
      } finally {
        setLoading(false);
      }
    }
  };

  return (
    <div className="bg-white rounded-lg shadow overflow-hidden">
      <button
        onClick={toggle}
        className="w-full flex items-center justify-between px-4 py-4 text-left hover:bg-gray-50 transition-colors"
      >
        <div>
          <p className="font-semibold text-gray-800">{site.name}</p>
          <p className="text-sm text-gray-500">
            {site.godownCode}
            {site.address && ` · ${site.address}`}
          </p>
        </div>
        <span className="text-gray-400 text-sm">{expanded ? "Hide shutters ▲" : "View shutters ▼"}</span>
      </button>
      {expanded && (
        <div className="bg-gray-50">
          {loading && <p className="px-4 py-3 text-sm text-gray-500">Loading shutter units…</p>}
          {error && <p className="px-4 py-3 text-sm text-red-600">{error}</p>}
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
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    api
      .get<Site[]>("/api/v1/sites")
      .then(setSites)
      .catch((err) => setError(err instanceof ApiError ? err.message : "Failed to load sites."));
  }, []);

  return (
    <div>
      <h1 className="text-2xl font-bold text-gray-800 mb-1">Godown Status</h1>
      <p className="text-gray-500 mb-6">Live shutter/lock status across every site in your organization.</p>

      {error && <p className="text-red-600 mb-4">{error}</p>}
      {sites === null && !error && <p className="text-gray-500">Loading sites…</p>}
      {sites?.length === 0 && <p className="text-gray-500">No sites configured yet.</p>}

      <div className="space-y-3">
        {sites?.map((site) => (
          <SiteCard key={site.id} site={site} />
        ))}
      </div>
    </div>
  );
}
