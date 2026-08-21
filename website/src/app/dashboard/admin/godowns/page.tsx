"use client";

import { useEffect, useState, FormEvent } from "react";
import { api, ApiError } from "@/lib/api";
import { Device, Site, ShutterUnit } from "@/lib/types";

// ---- Shutter units within one godown ----

function ShutterUnitsPanel({ siteId }: { siteId: number }) {
  const [units, setUnits] = useState<ShutterUnit[] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [label, setLabel] = useState("");
  const [submitting, setSubmitting] = useState(false);

  const load = () => api.get<ShutterUnit[]>(`/api/v1/sites/${siteId}/shutter-units`).then(setUnits);

  useEffect(() => {
    load().catch((err) => setError(err instanceof ApiError ? err.message : "Failed to load shutters."));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [siteId]);

  const handleAdd = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      await api.post(`/api/v1/sites/${siteId}/shutter-units`, { label });
      setLabel("");
      await load();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Failed to add shutter.");
    } finally {
      setSubmitting(false);
    }
  };

  const toggleStatus = async (unit: ShutterUnit) => {
    await api.put(`/api/v1/shutter-units/${unit.id}`, {
      label: unit.label,
      status: unit.status === "ACTIVE" ? "INACTIVE" : "ACTIVE",
    });
    await load();
  };

  return (
    <div className="bg-gray-50 border-t border-gray-100 px-4 py-4">
      {error && <p className="text-red-600 text-sm mb-3">{error}</p>}
      <form onSubmit={handleAdd} className="flex gap-2 mb-3">
        <input
          value={label}
          onChange={(e) => setLabel(e.target.value)}
          placeholder="Shutter label (e.g. Bay 1 Shutter)"
          required
          className="flex-1 px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-brand-red"
        />
        <button
          type="submit"
          disabled={submitting}
          className="px-4 py-2 bg-brand-red hover:bg-brand-red-dark disabled:opacity-60 text-white text-sm font-medium rounded-lg"
        >
          Add shutter
        </button>
      </form>
      <div className="bg-white rounded-lg divide-y divide-gray-100">
        {units?.map((unit) => (
          <div key={unit.id} className="flex items-center justify-between px-4 py-3">
            <div>
              <p className="text-sm font-medium text-gray-800">{unit.label}</p>
              <p className="text-xs text-gray-500">
                {unit.device ? `Mapped to ${unit.device.velosyssDeviceRef}` : "No device mapped — see Devices below"}
              </p>
            </div>
            <div className="flex items-center gap-3">
              <span className={`text-xs ${unit.status === "ACTIVE" ? "text-green-700" : "text-gray-400"}`}>
                {unit.status}
              </span>
              <button onClick={() => toggleStatus(unit)} className="text-xs text-brand-red hover:underline">
                {unit.status === "ACTIVE" ? "Deactivate" : "Reactivate"}
              </button>
            </div>
          </div>
        ))}
        {units?.length === 0 && <p className="px-4 py-3 text-sm text-gray-400">No shutters yet.</p>}
      </div>
    </div>
  );
}

// ---- Godowns (sites) ----

function GodownRow({ site, onUpdated }: { site: Site; onUpdated: () => void }) {
  const [editing, setEditing] = useState(false);
  const [expanded, setExpanded] = useState(false);
  const [name, setName] = useState(site.name);
  const [godownCode, setGodownCode] = useState(site.godownCode);
  const [address, setAddress] = useState(site.address ?? "");
  const [status, setStatus] = useState(site.status);
  const [error, setError] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);

  const handleSave = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    setSaving(true);
    try {
      await api.put(`/api/v1/sites/${site.id}`, { name, godownCode, address: address || null, status });
      setEditing(false);
      onUpdated();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Failed to save godown.");
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="border-t border-gray-100">
      <div className="flex items-center justify-between px-4 py-3">
        <div>
          <p className="font-medium text-gray-800">{site.name}</p>
          <p className="text-sm text-gray-500">
            {site.godownCode}
            {site.address && ` · ${site.address}`}
          </p>
        </div>
        <div className="flex items-center gap-3 shrink-0">
          <span className={`text-xs ${site.status === "ACTIVE" ? "text-green-700" : "text-gray-400"}`}>
            {site.status}
          </span>
          <button onClick={() => setEditing((v) => !v)} className="text-xs text-brand-red hover:underline">
            {editing ? "Cancel" : "Edit"}
          </button>
          <button onClick={() => setExpanded((v) => !v)} className="text-xs text-brand-green hover:underline">
            {expanded ? "Hide shutters" : "Shutters"}
          </button>
        </div>
      </div>

      {editing && (
        <form onSubmit={handleSave} className="px-4 pb-4 grid sm:grid-cols-2 gap-2">
          {error && <p className="sm:col-span-2 text-red-600 text-sm">{error}</p>}
          <input
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="Name"
            required
            className="px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-brand-red"
          />
          <input
            value={godownCode}
            onChange={(e) => setGodownCode(e.target.value)}
            placeholder="Godown code"
            required
            className="px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-brand-red"
          />
          <input
            value={address}
            onChange={(e) => setAddress(e.target.value)}
            placeholder="Address"
            className="px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-brand-red"
          />
          <select
            value={status}
            onChange={(e) => setStatus(e.target.value)}
            className="px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-brand-red"
          >
            <option value="ACTIVE">ACTIVE</option>
            <option value="INACTIVE">INACTIVE</option>
          </select>
          <button
            type="submit"
            disabled={saving}
            className="sm:col-span-2 px-4 py-2 bg-brand-red hover:bg-brand-red-dark disabled:opacity-60 text-white text-sm font-medium rounded-lg"
          >
            {saving ? "Saving…" : "Save"}
          </button>
        </form>
      )}

      {expanded && <ShutterUnitsPanel siteId={site.id} />}
    </div>
  );
}

function GodownsSection() {
  const [sites, setSites] = useState<Site[] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [name, setName] = useState("");
  const [godownCode, setGodownCode] = useState("");
  const [address, setAddress] = useState("");
  const [submitting, setSubmitting] = useState(false);

  const load = () => api.get<Site[]>("/api/v1/sites").then(setSites);

  useEffect(() => {
    load().catch((err) => setError(err instanceof ApiError ? err.message : "Failed to load godowns."));
  }, []);

  const handleAdd = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      await api.post("/api/v1/sites", { name, godownCode, address: address || null });
      setName("");
      setGodownCode("");
      setAddress("");
      await load();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Failed to add godown.");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="mb-8">
      <h2 className="text-lg font-semibold text-gray-800 mb-3">Godowns</h2>
      {error && <p className="text-red-600 text-sm mb-3">{error}</p>}

      <div className="bg-white rounded-lg shadow p-5 mb-4">
        <h3 className="text-sm font-semibold text-gray-700 mb-3">Add a godown</h3>
        <form onSubmit={handleAdd} className="grid sm:grid-cols-3 gap-2">
          <input
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="Name (e.g. Indore Godown)"
            required
            className="px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-brand-red"
          />
          <input
            value={godownCode}
            onChange={(e) => setGodownCode(e.target.value)}
            placeholder="Godown code (e.g. DC-IND-02)"
            required
            className="px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-brand-red"
          />
          <input
            value={address}
            onChange={(e) => setAddress(e.target.value)}
            placeholder="Address (optional)"
            className="px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-brand-red"
          />
          <button
            type="submit"
            disabled={submitting}
            className="sm:col-span-3 px-4 py-2 bg-brand-red hover:bg-brand-red-dark disabled:opacity-60 text-white text-sm font-medium rounded-lg"
          >
            {submitting ? "Adding…" : "Add godown"}
          </button>
        </form>
      </div>

      <div className="bg-white rounded-lg shadow">
        {sites?.map((site) => (
          <GodownRow key={site.id} site={site} onUpdated={load} />
        ))}
        {sites?.length === 0 && <p className="px-4 py-6 text-center text-gray-500">No godowns yet.</p>}
      </div>
    </div>
  );
}

// ---- Devices ----

interface FlatShutterOption {
  unit: ShutterUnit;
  siteName: string;
}

function DevicesSection() {
  const [devices, setDevices] = useState<Device[] | null>(null);
  const [shutterOptions, setShutterOptions] = useState<FlatShutterOption[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [ref, setRef] = useState("");
  const [shutterUnitId, setShutterUnitId] = useState<string>("");
  const [submitting, setSubmitting] = useState(false);

  const loadDevices = () => api.get<Device[]>("/api/v1/devices").then(setDevices);

  const loadShutterOptions = async () => {
    const sites = await api.get<Site[]>("/api/v1/sites");
    const perSite = await Promise.all(
      sites.map(async (site) => {
        const units = await api.get<ShutterUnit[]>(`/api/v1/sites/${site.id}/shutter-units`);
        return units.map((unit) => ({ unit, siteName: site.name }));
      }),
    );
    setShutterOptions(perSite.flat());
  };

  useEffect(() => {
    loadDevices().catch((err) => setError(err instanceof ApiError ? err.message : "Failed to load devices."));
    loadShutterOptions().catch(() => {
      // Non-fatal — the dropdown just stays empty; devices can still be registered unassigned.
    });
  }, []);

  const handleRegister = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      await api.post("/api/v1/devices", {
        velosyssDeviceRef: ref,
        shutterUnitId: shutterUnitId ? Number(shutterUnitId) : null,
      });
      setRef("");
      setShutterUnitId("");
      await Promise.all([loadDevices(), loadShutterOptions()]);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Failed to register device.");
    } finally {
      setSubmitting(false);
    }
  };

  const reassign = async (device: Device, newShutterUnitId: string) => {
    setError(null);
    try {
      await api.put(`/api/v1/devices/${device.id}`, {
        velosyssDeviceRef: device.velosyssDeviceRef,
        shutterUnitId: newShutterUnitId ? Number(newShutterUnitId) : null,
        status: device.status,
      });
      await Promise.all([loadDevices(), loadShutterOptions()]);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Failed to update mapping.");
    }
  };

  // A shutter already mapped to a *different* device shouldn't be offered as a target.
  const availableFor = (device: Device) =>
    shutterOptions.filter((o) => !o.unit.device || o.unit.device.id === device.id);

  return (
    <div>
      <h2 className="text-lg font-semibold text-gray-800 mb-3">Devices</h2>
      {error && <p className="text-red-600 text-sm mb-3">{error}</p>}

      <div className="bg-white rounded-lg shadow p-5 mb-4">
        <h3 className="text-sm font-semibold text-gray-700 mb-3">Register a device</h3>
        <form onSubmit={handleRegister} className="grid sm:grid-cols-3 gap-2">
          <input
            value={ref}
            onChange={(e) => setRef(e.target.value)}
            placeholder="Velosyss device ref (e.g. VLS-DL-0002)"
            required
            className="px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-brand-red"
          />
          <select
            value={shutterUnitId}
            onChange={(e) => setShutterUnitId(e.target.value)}
            className="px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-brand-red"
          >
            <option value="">Unassigned (map later)</option>
            {shutterOptions
              .filter((o) => !o.unit.device)
              .map((o) => (
                <option key={o.unit.id} value={o.unit.id}>
                  {o.siteName} — {o.unit.label}
                </option>
              ))}
          </select>
          <button
            type="submit"
            disabled={submitting}
            className="px-4 py-2 bg-brand-red hover:bg-brand-red-dark disabled:opacity-60 text-white text-sm font-medium rounded-lg"
          >
            {submitting ? "Registering…" : "Register device"}
          </button>
        </form>
      </div>

      <div className="bg-white rounded-lg shadow overflow-x-auto">
        <table className="w-full text-left">
          <thead className="bg-gray-50 text-xs uppercase text-gray-500">
            <tr>
              <th className="px-4 py-3">Device ref</th>
              <th className="px-4 py-3">Online</th>
              <th className="px-4 py-3">Battery</th>
              <th className="px-4 py-3">Mapped shutter</th>
            </tr>
          </thead>
          <tbody>
            {devices?.map((device) => {
              const current = shutterOptions.find((o) => o.unit.device?.id === device.id);
              return (
                <tr key={device.id} className="border-t border-gray-100">
                  <td className="px-4 py-3 text-sm font-mono text-gray-800">{device.velosyssDeviceRef}</td>
                  <td className="px-4 py-3 text-sm">
                    <span className={device.online ? "text-green-700" : "text-gray-400"}>
                      {device.online ? "Online" : "Offline"}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-sm text-gray-500">
                    {device.lastBatteryPct != null ? `${device.lastBatteryPct}%` : "—"}
                  </td>
                  <td className="px-4 py-3">
                    <select
                      value={current?.unit.id ?? ""}
                      onChange={(e) => reassign(device, e.target.value)}
                      className="text-sm border border-gray-200 rounded px-2 py-1"
                    >
                      <option value="">Unassigned</option>
                      {availableFor(device).map((o) => (
                        <option key={o.unit.id} value={o.unit.id}>
                          {o.siteName} — {o.unit.label}
                        </option>
                      ))}
                    </select>
                  </td>
                </tr>
              );
            })}
            {devices?.length === 0 && (
              <tr>
                <td colSpan={4} className="px-4 py-6 text-center text-gray-500">
                  No devices registered yet.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}

export default function GodownsAdminPage() {
  return (
    <div>
      <h1 className="text-2xl font-bold text-gray-800 mb-1">Godowns</h1>
      <p className="text-gray-500 mb-6">
        Add and edit godowns and shutters, and register Digital Lock devices and map them to a shutter.
      </p>
      <GodownsSection />
      <DevicesSection />
    </div>
  );
}
