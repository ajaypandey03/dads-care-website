"use client";

import { useEffect, useState, FormEvent } from "react";
import { api, ApiError } from "@/lib/api";
import { useAuth } from "@/context/AuthContext";
import { ProductMaster, TransporterMaster } from "@/lib/types";

function ProductMasters({ canManage }: { canManage: boolean }) {
  const [items, setItems] = useState<ProductMaster[] | null>(null);
  const [name, setName] = useState("");
  const [unit, setUnit] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const load = () => api.get<ProductMaster[]>("/api/v1/product-masters").then(setItems);

  useEffect(() => {
    load().catch((err) => setError(err instanceof ApiError ? err.message : "Failed to load products."));
  }, []);

  const handleAdd = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      await api.post("/api/v1/product-masters", { name, unit, active: true });
      setName("");
      setUnit("");
      await load();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Failed to add product.");
    } finally {
      setSubmitting(false);
    }
  };

  // One-way: GET /api/v1/product-masters only ever returns active=true rows (see
  // MasterDataService.listProducts), so a deactivated item drops out of this list
  // immediately — there's no "reactivate" affordance to offer here yet.
  const remove = async (item: ProductMaster) => {
    await api.delete(`/api/v1/product-masters/${item.id}`);
    await load();
  };

  return (
    <div className="bg-white rounded-lg shadow p-5">
      <h2 className="text-lg font-semibold text-gray-800 mb-4">Product Masters</h2>
      {error && <p className="text-red-600 text-sm mb-3">{error}</p>}
      {canManage && (
        <form onSubmit={handleAdd} className="flex gap-2 mb-4">
          <input
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="Product name"
            required
            className="flex-1 px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-brand-red"
          />
          <input
            value={unit}
            onChange={(e) => setUnit(e.target.value)}
            placeholder="Unit (e.g. bags)"
            required
            className="w-40 px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-brand-red"
          />
          <button
            type="submit"
            disabled={submitting}
            className="px-4 py-2 bg-brand-red hover:bg-brand-red-dark disabled:opacity-60 text-white text-sm font-medium rounded-lg"
          >
            Add
          </button>
        </form>
      )}
      <div className="divide-y divide-gray-100">
        {items?.map((item) => (
          <div key={item.id} className="flex items-center justify-between py-2">
            <span className="text-gray-800">
              {item.name} <span className="text-gray-400">· {item.unit}</span>
            </span>
            {canManage && (
              <button onClick={() => remove(item)} className="text-xs text-red-600 hover:underline">
                Remove
              </button>
            )}
          </div>
        ))}
        {items?.length === 0 && <p className="text-sm text-gray-400 py-2">No products yet.</p>}
      </div>
    </div>
  );
}

function TransporterMasters({ canManage }: { canManage: boolean }) {
  const [items, setItems] = useState<TransporterMaster[] | null>(null);
  const [name, setName] = useState("");
  const [code, setCode] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const load = () => api.get<TransporterMaster[]>("/api/v1/transporter-masters").then(setItems);

  useEffect(() => {
    load().catch((err) => setError(err instanceof ApiError ? err.message : "Failed to load transporters."));
  }, []);

  const handleAdd = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      await api.post("/api/v1/transporter-masters", { name, code: code || null, active: true });
      setName("");
      setCode("");
      await load();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Failed to add transporter.");
    } finally {
      setSubmitting(false);
    }
  };

  // Same one-way limitation as ProductMasters above — see that comment.
  const remove = async (item: TransporterMaster) => {
    await api.delete(`/api/v1/transporter-masters/${item.id}`);
    await load();
  };

  return (
    <div className="bg-white rounded-lg shadow p-5">
      <h2 className="text-lg font-semibold text-gray-800 mb-4">Transporter Masters</h2>
      {error && <p className="text-red-600 text-sm mb-3">{error}</p>}
      {canManage && (
        <form onSubmit={handleAdd} className="flex gap-2 mb-4">
          <input
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="Transporter name"
            required
            className="flex-1 px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-brand-red"
          />
          <input
            value={code}
            onChange={(e) => setCode(e.target.value)}
            placeholder="Code (optional)"
            className="w-40 px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-brand-red"
          />
          <button
            type="submit"
            disabled={submitting}
            className="px-4 py-2 bg-brand-red hover:bg-brand-red-dark disabled:opacity-60 text-white text-sm font-medium rounded-lg"
          >
            Add
          </button>
        </form>
      )}
      <div className="divide-y divide-gray-100">
        {items?.map((item) => (
          <div key={item.id} className="flex items-center justify-between py-2">
            <span className="text-gray-800">
              {item.name} {item.code && <span className="text-gray-400">· {item.code}</span>}
            </span>
            {canManage && (
              <button onClick={() => remove(item)} className="text-xs text-red-600 hover:underline">
                Remove
              </button>
            )}
          </div>
        ))}
        {items?.length === 0 && <p className="text-sm text-gray-400 py-2">No transporters yet.</p>}
      </div>
    </div>
  );
}

export default function MasterDataPage() {
  const { canManage } = useAuth();
  return (
    <div>
      <h1 className="text-2xl font-bold text-gray-800 mb-1">Master Data</h1>
      <p className="text-gray-500 mb-6">
        Products and transporters available to operators when filling Opening/Closing forms in the app.
        {!canManage && " You have view-only access."}
      </p>
      <div className="grid md:grid-cols-2 gap-6">
        <ProductMasters canManage={canManage} />
        <TransporterMasters canManage={canManage} />
      </div>
    </div>
  );
}
