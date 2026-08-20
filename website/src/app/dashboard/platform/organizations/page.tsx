"use client";

import { useEffect, useState, FormEvent } from "react";
import { api, ApiError } from "@/lib/api";
import { useAuth } from "@/context/AuthContext";
import { CreateOrganizationResponse, Organization } from "@/lib/types";

function slugify(name: string): string {
  return name
    .toLowerCase()
    .trim()
    .replace(/[^a-z0-9]+/g, "-")
    .replace(/^-+|-+$/g, "");
}

export default function PlatformOrganizationsPage() {
  const { isPlatformAdmin, loading: authLoading } = useAuth();

  const [orgs, setOrgs] = useState<Organization[] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [justCreated, setJustCreated] = useState<CreateOrganizationResponse | null>(null);

  const [name, setName] = useState("");
  const [slug, setSlug] = useState("");
  const [slugTouched, setSlugTouched] = useState(false);
  const [codePrefix, setCodePrefix] = useState("");
  const [adminName, setAdminName] = useState("");
  const [adminEmail, setAdminEmail] = useState("");
  const [adminPhone, setAdminPhone] = useState("");

  const load = () => api.get<Organization[]>("/api/v1/platform/organizations").then(setOrgs);

  useEffect(() => {
    if (!isPlatformAdmin) return;
    load().catch((err) => setError(err instanceof ApiError ? err.message : "Failed to load organizations."));
  }, [isPlatformAdmin]);

  if (authLoading) {
    return <p className="text-gray-500">Loading…</p>;
  }

  if (!isPlatformAdmin) {
    return (
      <div className="bg-white rounded-lg shadow p-6">
        <h1 className="text-xl font-bold text-gray-800 mb-2">Not available</h1>
        <p className="text-gray-500">
          This screen is restricted to Dad&apos;s Care platform admins. Your account doesn&apos;t have that access.
        </p>
      </div>
    );
  }

  const handleNameChange = (value: string) => {
    setName(value);
    if (!slugTouched) {
      setSlug(slugify(value));
    }
  };

  const handleCreate = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    setJustCreated(null);
    setSubmitting(true);
    try {
      const response = await api.post<CreateOrganizationResponse>("/api/v1/platform/organizations", {
        name,
        slug,
        codePrefix: codePrefix.toUpperCase(),
        adminName,
        adminEmail,
        adminPhone: adminPhone || null,
      });
      setJustCreated(response);
      setName("");
      setSlug("");
      setSlugTouched(false);
      setCodePrefix("");
      setAdminName("");
      setAdminEmail("");
      setAdminPhone("");
      await load();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Failed to create organization.");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div>
      <h1 className="text-2xl font-bold text-gray-800 mb-1">Platform — Customer Organizations</h1>
      <p className="text-gray-500 mb-6">
        Onboard a new Dad&apos;s Care customer as its own organization, with its first admin user.
      </p>

      {error && <p className="text-red-600 mb-4">{error}</p>}

      {justCreated && (
        <div className="mb-6 rounded-lg bg-amber-50 border border-amber-200 px-4 py-3 text-sm text-amber-800">
          <p className="font-semibold mb-1">
            {justCreated.organization.name} was created. Share this temporary password with{" "}
            {justCreated.adminUser.email} directly (it won&apos;t be shown again):
          </p>
          <code className="bg-white px-2 py-1 rounded border border-amber-200 text-amber-900">
            {justCreated.temporaryPassword}
          </code>
        </div>
      )}

      <div className="bg-white rounded-lg shadow p-5 mb-6">
        <h2 className="text-lg font-semibold text-gray-800 mb-4">Onboard a new organization</h2>
        <form onSubmit={handleCreate} className="grid sm:grid-cols-2 gap-3">
          <input
            value={name}
            onChange={(e) => handleNameChange(e.target.value)}
            placeholder="Organization name (e.g. Beta Logistics Pvt Ltd)"
            required
            className="sm:col-span-2 px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-blue-500"
          />
          <input
            value={slug}
            onChange={(e) => {
              setSlug(e.target.value);
              setSlugTouched(true);
            }}
            placeholder="slug (e.g. beta-logistics)"
            required
            pattern="^[a-z0-9]+(-[a-z0-9]+)*$"
            title="lowercase-kebab-case, e.g. beta-logistics"
            className="px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-blue-500"
          />
          <input
            value={codePrefix}
            onChange={(e) => setCodePrefix(e.target.value.toUpperCase())}
            placeholder="Code prefix (e.g. BL)"
            required
            maxLength={10}
            className="px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-blue-500"
          />
          <input
            value={adminName}
            onChange={(e) => setAdminName(e.target.value)}
            placeholder="First admin's name"
            required
            className="px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-blue-500"
          />
          <input
            value={adminEmail}
            onChange={(e) => setAdminEmail(e.target.value)}
            type="email"
            placeholder="First admin's email"
            required
            className="px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-blue-500"
          />
          <input
            value={adminPhone}
            onChange={(e) => setAdminPhone(e.target.value)}
            placeholder="First admin's phone (optional)"
            className="sm:col-span-2 px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-blue-500"
          />
          <button
            type="submit"
            disabled={submitting}
            className="sm:col-span-2 px-4 py-2 bg-blue-700 hover:bg-blue-800 disabled:opacity-60 text-white text-sm font-medium rounded-lg"
          >
            {submitting ? "Creating…" : "Create organization"}
          </button>
        </form>
      </div>

      <div className="bg-white rounded-lg shadow overflow-x-auto">
        <table className="w-full text-left">
          <thead className="bg-gray-50 text-xs uppercase text-gray-500">
            <tr>
              <th className="px-4 py-3">Name</th>
              <th className="px-4 py-3">Slug</th>
              <th className="px-4 py-3">Code prefix</th>
              <th className="px-4 py-3">Status</th>
              <th className="px-4 py-3">Onboarded</th>
            </tr>
          </thead>
          <tbody>
            {orgs?.map((org) => (
              <tr key={org.id} className="border-t border-gray-100">
                <td className="px-4 py-3 text-sm text-gray-800">{org.name}</td>
                <td className="px-4 py-3 text-sm text-gray-500 font-mono">{org.slug}</td>
                <td className="px-4 py-3 text-sm text-gray-500 font-mono">{org.codePrefix}</td>
                <td className="px-4 py-3 text-sm">
                  <span className={org.active ? "text-green-700" : "text-gray-400"}>
                    {org.active ? "Active" : "Inactive"}
                  </span>
                </td>
                <td className="px-4 py-3 text-sm text-gray-500">{new Date(org.createdAt).toLocaleDateString()}</td>
              </tr>
            ))}
            {orgs?.length === 0 && (
              <tr>
                <td colSpan={5} className="px-4 py-6 text-center text-gray-500">
                  No organizations yet.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
