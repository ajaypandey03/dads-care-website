"use client";

import { useEffect, useState, FormEvent } from "react";
import { api, ApiError } from "@/lib/api";
import { useAuth } from "@/context/AuthContext";
import { CommandType, ProductMaster, Site, ShutterUnit, TransporterMaster, UnlockRequest } from "@/lib/types";

interface StockLineRow {
  productMasterId: string;
  quantity: string;
}

interface TruckEntryRow {
  source: string;
  productMasterId: string;
  vehicleNo: string;
  transporterMasterId: string;
  quantity: string;
}

interface CustomFieldRow {
  heading: string;
  value: string;
}

const MAX_CUSTOM_FIELDS = 10;

function OperateForm({
  site,
  unit,
  commandType,
  onDone,
  onCancel,
}: {
  site: Site;
  unit: ShutterUnit;
  commandType: CommandType;
  onDone: () => void;
  onCancel: () => void;
}) {
  const [products, setProducts] = useState<ProductMaster[]>([]);
  const [transporters, setTransporters] = useState<TransporterMaster[]>([]);
  const [stockLines, setStockLines] = useState<StockLineRow[]>([]);
  const [truckEntries, setTruckEntries] = useState<TruckEntryRow[]>([]);
  const [customFields, setCustomFields] = useState<CustomFieldRow[]>([]);
  const [laborCount, setLaborCount] = useState("");
  const [remarks, setRemarks] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [result, setResult] = useState<UnlockRequest | null>(null);

  useEffect(() => {
    api.get<ProductMaster[]>("/api/v1/product-masters").then(setProducts).catch(() => {});
    api.get<TransporterMaster[]>("/api/v1/transporter-masters").then(setTransporters).catch(() => {});
  }, []);

  const addStockLine = () => setStockLines((rows) => [...rows, { productMasterId: "", quantity: "" }]);
  const removeStockLine = (i: number) => setStockLines((rows) => rows.filter((_, idx) => idx !== i));

  const addTruckEntry = () =>
    setTruckEntries((rows) => [
      ...rows,
      { source: "", productMasterId: "", vehicleNo: "", transporterMasterId: "", quantity: "" },
    ]);
  const removeTruckEntry = (i: number) => setTruckEntries((rows) => rows.filter((_, idx) => idx !== i));

  const addCustomField = () => setCustomFields((rows) => [...rows, { heading: "", value: "" }]);
  const removeCustomField = (i: number) => setCustomFields((rows) => rows.filter((_, idx) => idx !== i));

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      const response = await api.post<UnlockRequest>(`/api/v1/devices/${unit.device!.id}/unlock-requests`, {
        commandType,
        stockLines: stockLines
          .filter((r) => r.productMasterId && r.quantity)
          .map((r) => ({ productMasterId: Number(r.productMasterId), quantity: Number(r.quantity) })),
        truckEntries: truckEntries
          .filter((r) => r.source && r.productMasterId && r.vehicleNo && r.transporterMasterId && r.quantity)
          .map((r) => ({
            source: r.source,
            productMasterId: Number(r.productMasterId),
            vehicleNo: r.vehicleNo,
            transporterMasterId: Number(r.transporterMasterId),
            quantity: Number(r.quantity),
          })),
        laborCount: laborCount ? Number(laborCount) : null,
        remarks: remarks || null,
        customFields: customFields.filter((r) => r.heading && r.value),
      });
      setResult(response);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Failed to submit the request.");
    } finally {
      setSubmitting(false);
    }
  };

  if (result) {
    const statusStyle =
      result.status === "RELAYED"
        ? "bg-green-50 border-green-200 text-green-800"
        : result.status === "PENDING"
          ? "bg-amber-50 border-amber-200 text-amber-800"
          : "bg-red-50 border-red-200 text-red-800";
    return (
      <div className={`rounded-lg border px-4 py-4 ${statusStyle}`}>
        <p className="font-semibold mb-1">
          {result.status === "RELAYED" && "Sent — the command was relayed to the lock."}
          {result.status === "PENDING" && "Submitted — waiting to be relayed."}
          {result.status === "FAILED" && "The command could not be relayed."}
        </p>
        <p className="text-sm mb-3">
          {commandType === "UNLOCK" ? "Open" : "Close"} request for {unit.label} at {site.name}.
        </p>
        <button onClick={onDone} className="text-sm px-4 py-2 rounded-lg bg-brand-red hover:bg-brand-red-dark text-white font-medium">
          Done
        </button>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-lg shadow p-5">
      <div className="flex items-center justify-between mb-4">
        <h2 className="text-lg font-semibold text-gray-800">
          {commandType === "UNLOCK" ? "Opening" : "Closing"} Form — {unit.label} ({site.name})
        </h2>
        <button onClick={onCancel} className="text-sm text-gray-500 hover:text-brand-red">
          Cancel
        </button>
      </div>
      {error && <p className="text-red-600 text-sm mb-3">{error}</p>}

      <form onSubmit={handleSubmit} className="space-y-6">
        <div>
          <div className="flex items-center justify-between mb-2">
            <h3 className="text-sm font-semibold text-gray-700">Stock lines</h3>
            <button type="button" onClick={addStockLine} className="text-xs text-brand-red hover:underline">
              + Add stock line
            </button>
          </div>
          <div className="space-y-2">
            {stockLines.map((row, i) => (
              <div key={i} className="grid grid-cols-[1fr_120px_auto] gap-2">
                <select
                  value={row.productMasterId}
                  onChange={(e) =>
                    setStockLines((rows) => rows.map((r, idx) => (idx === i ? { ...r, productMasterId: e.target.value } : r)))
                  }
                  required
                  className="px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-brand-red"
                >
                  <option value="">Select product</option>
                  {products.map((p) => (
                    <option key={p.id} value={p.id}>
                      {p.name} ({p.unit})
                    </option>
                  ))}
                </select>
                <input
                  type="number"
                  min={1}
                  value={row.quantity}
                  onChange={(e) =>
                    setStockLines((rows) => rows.map((r, idx) => (idx === i ? { ...r, quantity: e.target.value } : r)))
                  }
                  placeholder="Qty"
                  required
                  className="px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-brand-red"
                />
                <button type="button" onClick={() => removeStockLine(i)} className="text-xs text-red-600 hover:underline">
                  Remove
                </button>
              </div>
            ))}
            {stockLines.length === 0 && <p className="text-sm text-gray-400">No stock lines added.</p>}
          </div>
        </div>

        <div>
          <div className="flex items-center justify-between mb-2">
            <h3 className="text-sm font-semibold text-gray-700">Truck entries</h3>
            <button type="button" onClick={addTruckEntry} className="text-xs text-brand-red hover:underline">
              + Add truck entry
            </button>
          </div>
          <div className="space-y-3">
            {truckEntries.map((row, i) => (
              <div key={i} className="border border-gray-200 rounded-lg p-3 grid sm:grid-cols-2 gap-2">
                <input
                  value={row.source}
                  onChange={(e) =>
                    setTruckEntries((rows) => rows.map((r, idx) => (idx === i ? { ...r, source: e.target.value } : r)))
                  }
                  placeholder="Source"
                  required
                  className="px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-brand-red"
                />
                <input
                  value={row.vehicleNo}
                  onChange={(e) =>
                    setTruckEntries((rows) => rows.map((r, idx) => (idx === i ? { ...r, vehicleNo: e.target.value } : r)))
                  }
                  placeholder="Vehicle no."
                  required
                  className="px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-brand-red"
                />
                <select
                  value={row.productMasterId}
                  onChange={(e) =>
                    setTruckEntries((rows) =>
                      rows.map((r, idx) => (idx === i ? { ...r, productMasterId: e.target.value } : r)),
                    )
                  }
                  required
                  className="px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-brand-red"
                >
                  <option value="">Select product</option>
                  {products.map((p) => (
                    <option key={p.id} value={p.id}>
                      {p.name} ({p.unit})
                    </option>
                  ))}
                </select>
                <select
                  value={row.transporterMasterId}
                  onChange={(e) =>
                    setTruckEntries((rows) =>
                      rows.map((r, idx) => (idx === i ? { ...r, transporterMasterId: e.target.value } : r)),
                    )
                  }
                  required
                  className="px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-brand-red"
                >
                  <option value="">Select transporter</option>
                  {transporters.map((t) => (
                    <option key={t.id} value={t.id}>
                      {t.name}
                    </option>
                  ))}
                </select>
                <input
                  type="number"
                  min={1}
                  value={row.quantity}
                  onChange={(e) =>
                    setTruckEntries((rows) => rows.map((r, idx) => (idx === i ? { ...r, quantity: e.target.value } : r)))
                  }
                  placeholder="Qty"
                  required
                  className="px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-brand-red"
                />
                <button
                  type="button"
                  onClick={() => removeTruckEntry(i)}
                  className="text-xs text-red-600 hover:underline text-left sm:self-center"
                >
                  Remove entry
                </button>
              </div>
            ))}
            {truckEntries.length === 0 && <p className="text-sm text-gray-400">No truck entries added.</p>}
          </div>
        </div>

        <div className="grid sm:grid-cols-2 gap-3">
          <input
            type="number"
            min={0}
            value={laborCount}
            onChange={(e) => setLaborCount(e.target.value)}
            placeholder="Labor count (optional)"
            className="px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-brand-red"
          />
          <input
            value={remarks}
            onChange={(e) => setRemarks(e.target.value)}
            placeholder="Remarks (optional)"
            maxLength={200}
            className="px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-brand-red"
          />
        </div>

        <div>
          <div className="flex items-center justify-between mb-2">
            <h3 className="text-sm font-semibold text-gray-700">Custom fields</h3>
            {customFields.length < MAX_CUSTOM_FIELDS && (
              <button type="button" onClick={addCustomField} className="text-xs text-brand-red hover:underline">
                + Add custom field
              </button>
            )}
          </div>
          <div className="space-y-2">
            {customFields.map((row, i) => (
              <div key={i} className="grid grid-cols-[1fr_1fr_auto] gap-2">
                <input
                  value={row.heading}
                  onChange={(e) =>
                    setCustomFields((rows) => rows.map((r, idx) => (idx === i ? { ...r, heading: e.target.value } : r)))
                  }
                  placeholder="Field name"
                  required
                  className="px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-brand-red"
                />
                <input
                  value={row.value}
                  onChange={(e) =>
                    setCustomFields((rows) => rows.map((r, idx) => (idx === i ? { ...r, value: e.target.value } : r)))
                  }
                  placeholder="Value"
                  required
                  className="px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-brand-red"
                />
                <button type="button" onClick={() => removeCustomField(i)} className="text-xs text-red-600 hover:underline">
                  Remove
                </button>
              </div>
            ))}
          </div>
        </div>

        <button
          type="submit"
          disabled={submitting}
          className="w-full px-4 py-3 bg-brand-red hover:bg-brand-red-dark disabled:opacity-60 text-white font-semibold rounded-lg"
        >
          {submitting ? "Submitting…" : commandType === "UNLOCK" ? "Submit & Open Shutter" : "Submit & Close Shutter"}
        </button>
      </form>
    </div>
  );
}

function GodownPicker({ onPick }: { onPick: (site: Site, unit: ShutterUnit, commandType: CommandType) => void }) {
  const [sites, setSites] = useState<Site[] | null>(null);
  const [selectedSiteId, setSelectedSiteId] = useState<number | null>(null);
  const [units, setUnits] = useState<ShutterUnit[] | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    api
      .get<Site[]>("/api/v1/sites")
      .then(setSites)
      .catch((err) => setError(err instanceof ApiError ? err.message : "Failed to load godowns."));
  }, []);

  useEffect(() => {
    if (selectedSiteId === null) {
      // No async gap needed, but setState still can't run synchronously inside the
      // effect body (react-hooks/set-state-in-effect) — defer to a microtask instead.
      queueMicrotask(() => setUnits(null));
      return;
    }
    api
      .get<ShutterUnit[]>(`/api/v1/sites/${selectedSiteId}/shutter-units`)
      .then(setUnits)
      .catch((err) => setError(err instanceof ApiError ? err.message : "Failed to load shutters."));
  }, [selectedSiteId]);

  const selectedSite = sites?.find((s) => s.id === selectedSiteId) ?? null;

  return (
    <div>
      {error && <p className="text-red-600 mb-4">{error}</p>}
      {sites === null && !error && <p className="text-gray-500">Loading godowns…</p>}
      {sites?.length === 0 && <p className="text-gray-500">No godowns configured yet.</p>}

      {sites && sites.length > 0 && (
        <div className="bg-white rounded-lg shadow p-4 mb-4">
          <label className="block text-sm font-medium text-gray-700 mb-2">Godown</label>
          <select
            value={selectedSiteId ?? ""}
            onChange={(e) => setSelectedSiteId(e.target.value ? Number(e.target.value) : null)}
            className="w-full max-w-sm px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-brand-red"
          >
            <option value="">Select a godown…</option>
            {sites.map((s) => (
              <option key={s.id} value={s.id}>
                {s.name}
              </option>
            ))}
          </select>
        </div>
      )}

      {selectedSite && units && (
        <div className="bg-white rounded-lg shadow divide-y divide-gray-100">
          {units.map((unit) => (
            <div key={unit.id} className="flex items-center justify-between px-4 py-3">
              <div>
                <p className="font-medium text-gray-800">{unit.label}</p>
                <p className="text-xs text-gray-500">
                  {unit.device ? unit.device.velosyssDeviceRef : "No device mapped"} · Currently {unit.currentState}
                </p>
              </div>
              {unit.device ? (
                <div className="flex gap-2">
                  <button
                    onClick={() => onPick(selectedSite, unit, "UNLOCK")}
                    className="text-xs px-3 py-1.5 rounded-lg bg-brand-red hover:bg-brand-red-dark text-white font-medium"
                  >
                    Open
                  </button>
                  <button
                    onClick={() => onPick(selectedSite, unit, "LOCK")}
                    className="text-xs px-3 py-1.5 rounded-lg bg-brand-green hover:bg-brand-green-dark text-white font-medium"
                  >
                    Close
                  </button>
                </div>
              ) : (
                <span className="text-xs text-gray-400">No device mapped — ask an admin to map one</span>
              )}
            </div>
          ))}
          {units.length === 0 && <p className="px-4 py-3 text-sm text-gray-400">No shutters at this godown yet.</p>}
        </div>
      )}
    </div>
  );
}

export default function OperatePage() {
  const { canOperate, loading } = useAuth();
  const [selection, setSelection] = useState<{ site: Site; unit: ShutterUnit; commandType: CommandType } | null>(null);

  if (loading) {
    return <p className="text-gray-500">Loading…</p>;
  }

  if (!canOperate) {
    return (
      <div className="bg-white rounded-lg shadow p-6">
        <h1 className="text-xl font-bold text-gray-800 mb-2">Not available</h1>
        <p className="text-gray-500">
          Operating a shutter is restricted to Org Admins, Site Managers, and Operators. Your account doesn&apos;t
          have that access.
        </p>
      </div>
    );
  }

  return (
    <div>
      <h1 className="text-2xl font-bold text-gray-800 mb-1">Operate Shutter</h1>
      <p className="text-gray-500 mb-6">
        Pick a godown and shutter, fill in the Opening or Closing form, and submit to send the command to the lock.
      </p>

      {selection ? (
        <OperateForm
          site={selection.site}
          unit={selection.unit}
          commandType={selection.commandType}
          onDone={() => setSelection(null)}
          onCancel={() => setSelection(null)}
        />
      ) : (
        <GodownPicker onPick={(site, unit, commandType) => setSelection({ site, unit, commandType })} />
      )}
    </div>
  );
}
