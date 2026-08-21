"use client";

import { Suspense, useEffect, useState, FormEvent } from "react";
import Link from "next/link";
import { useSearchParams } from "next/navigation";
import { api, ApiError } from "@/lib/api";
import { useAuth } from "@/context/AuthContext";
import { CreateUserResponse, Organization, ResetPasswordResponse, Role, UserAdmin } from "@/lib/types";
import { PasswordMode, PasswordModeField } from "@/components/dashboard/PasswordModeField";

const ROLES: Role[] = ["ORG_ADMIN", "SITE_MANAGER", "OPERATOR", "VIEWER"];

function OrganizationDetailsForm({ org, onUpdated }: { org: Organization; onUpdated: (org: Organization) => void }) {
  const [name, setName] = useState(org.name);
  const [codePrefix, setCodePrefix] = useState(org.codePrefix);
  const [active, setActive] = useState(org.active);
  const [error, setError] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);
  const [saved, setSaved] = useState(false);

  const handleSave = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    setSaved(false);
    setSaving(true);
    try {
      const updated = await api.put<Organization>(`/api/v1/platform/organizations/${org.id}`, {
        name,
        codePrefix: codePrefix.toUpperCase(),
        active,
      });
      onUpdated(updated);
      setSaved(true);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Failed to save organization.");
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="bg-white rounded-lg shadow p-5 mb-6">
      <h2 className="text-lg font-semibold text-gray-800 mb-4">Organization details</h2>
      {error && <p className="text-red-600 text-sm mb-3">{error}</p>}
      {saved && <p className="text-green-700 text-sm mb-3">Saved.</p>}
      <form onSubmit={handleSave} className="grid sm:grid-cols-2 gap-3">
        <input
          value={name}
          onChange={(e) => {
            setName(e.target.value);
            setSaved(false);
          }}
          placeholder="Organization name"
          required
          className="sm:col-span-2 px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-brand-red"
        />
        <div className="px-3 py-2 border border-gray-200 rounded-lg text-sm text-gray-400 bg-gray-50">
          Slug: <span className="font-mono">{org.slug}</span> (fixed)
        </div>
        <input
          value={codePrefix}
          onChange={(e) => {
            setCodePrefix(e.target.value.toUpperCase());
            setSaved(false);
          }}
          placeholder="Code prefix"
          required
          maxLength={10}
          className="px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-brand-red"
        />
        <label className="sm:col-span-2 flex items-center gap-2 text-sm text-gray-700">
          <input
            type="checkbox"
            checked={active}
            onChange={(e) => {
              setActive(e.target.checked);
              setSaved(false);
            }}
            className="rounded border-gray-300"
          />
          Organization is active
        </label>
        <button
          type="submit"
          disabled={saving}
          className="sm:col-span-2 px-4 py-2 bg-brand-red hover:bg-brand-red-dark disabled:opacity-60 text-white text-sm font-medium rounded-lg"
        >
          {saving ? "Saving…" : "Save changes"}
        </button>
      </form>
    </div>
  );
}

function OrganizationUsers({ organizationId }: { organizationId: number }) {
  const [users, setUsers] = useState<UserAdmin[] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [justCreated, setJustCreated] = useState<CreateUserResponse | null>(null);
  const [justReset, setJustReset] = useState<ResetPasswordResponse | null>(null);

  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [phone, setPhone] = useState("");
  const [role, setRole] = useState<Role>("VIEWER");
  const [passwordMode, setPasswordMode] = useState<PasswordMode>("generate");
  const [password, setPassword] = useState("");

  const load = () =>
    api.get<UserAdmin[]>(`/api/v1/platform/organizations/${organizationId}/users`).then(setUsers);

  useEffect(() => {
    load().catch((err) => setError(err instanceof ApiError ? err.message : "Failed to load users."));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [organizationId]);

  const handleInvite = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    setJustCreated(null);
    setJustReset(null);
    setSubmitting(true);
    try {
      const response = await api.post<CreateUserResponse>(`/api/v1/platform/organizations/${organizationId}/users`, {
        name,
        email,
        phone: phone || null,
        role,
        password: passwordMode === "manual" ? password : null,
      });
      setJustCreated(response);
      setName("");
      setEmail("");
      setPhone("");
      setRole("VIEWER");
      setPasswordMode("generate");
      setPassword("");
      await load();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Failed to add user.");
    } finally {
      setSubmitting(false);
    }
  };

  const updateUser = async (user: UserAdmin, patch: Partial<Pick<UserAdmin, "role" | "status">>) => {
    await api.put(`/api/v1/platform/organizations/${organizationId}/users/${user.id}`, {
      role: patch.role ?? user.role,
      status: patch.status ?? user.status,
    });
    await load();
  };

  const resetPassword = async (user: UserAdmin) => {
    setJustCreated(null);
    setJustReset(null);
    try {
      const response = await api.post<ResetPasswordResponse>(
        `/api/v1/platform/organizations/${organizationId}/users/${user.id}/reset-password`,
      );
      setJustReset(response);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Failed to reset password.");
    }
  };

  return (
    <div>
      {error && <p className="text-red-600 mb-4">{error}</p>}

      {justCreated && (
        <div className="mb-6 rounded-lg bg-amber-50 border border-amber-200 px-4 py-3 text-sm text-amber-800">
          {justCreated.temporaryPassword ? (
            <>
              <p className="font-semibold mb-1">
                {justCreated.user.name} was added — share this temporary password with them directly (it won&apos;t
                be shown again):
              </p>
              <code className="bg-white px-2 py-1 rounded border border-amber-200 text-amber-900">
                {justCreated.temporaryPassword}
              </code>
            </>
          ) : (
            <p className="font-semibold">
              {justCreated.user.name} was added with the password you set — let them know it directly.
            </p>
          )}
        </div>
      )}

      {justReset && (
        <div className="mb-6 rounded-lg bg-amber-50 border border-amber-200 px-4 py-3 text-sm text-amber-800">
          <p className="font-semibold mb-1">
            Password reset for {justReset.user.email} — share this new temporary password with them directly (it
            won&apos;t be shown again):
          </p>
          <code className="bg-white px-2 py-1 rounded border border-amber-200 text-amber-900">
            {justReset.temporaryPassword}
          </code>
        </div>
      )}

      <div className="bg-white rounded-lg shadow p-5 mb-6">
        <h2 className="text-lg font-semibold text-gray-800 mb-4">Add a user to this organization</h2>
        <form onSubmit={handleInvite} className="grid sm:grid-cols-2 gap-3">
          <input
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="Name"
            required
            className="px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-brand-red"
          />
          <input
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            type="email"
            placeholder="Email"
            required
            className="px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-brand-red"
          />
          <input
            value={phone}
            onChange={(e) => setPhone(e.target.value)}
            placeholder="Phone (optional)"
            className="px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-brand-red"
          />
          <select
            value={role}
            onChange={(e) => setRole(e.target.value as Role)}
            className="px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-brand-red"
          >
            {ROLES.map((r) => (
              <option key={r} value={r}>
                {r}
              </option>
            ))}
          </select>
          <PasswordModeField
            mode={passwordMode}
            onModeChange={setPasswordMode}
            password={password}
            onPasswordChange={setPassword}
          />
          <button
            type="submit"
            disabled={submitting}
            className="sm:col-span-2 px-4 py-2 bg-brand-red hover:bg-brand-red-dark disabled:opacity-60 text-white text-sm font-medium rounded-lg"
          >
            {submitting ? "Adding…" : "Add user"}
          </button>
        </form>
      </div>

      <div className="bg-white rounded-lg shadow overflow-x-auto">
        <table className="w-full text-left">
          <thead className="bg-gray-50 text-xs uppercase text-gray-500">
            <tr>
              <th className="px-4 py-3">Name</th>
              <th className="px-4 py-3">Email</th>
              <th className="px-4 py-3">Role</th>
              <th className="px-4 py-3">Status</th>
              <th className="px-4 py-3"></th>
            </tr>
          </thead>
          <tbody>
            {users?.map((u) => (
              <tr key={u.id} className="border-t border-gray-100">
                <td className="px-4 py-3 text-sm text-gray-800">{u.name}</td>
                <td className="px-4 py-3 text-sm text-gray-500">{u.email}</td>
                <td className="px-4 py-3">
                  <select
                    value={u.role}
                    onChange={(e) => updateUser(u, { role: e.target.value as Role })}
                    className="text-sm border border-gray-200 rounded px-2 py-1"
                  >
                    {ROLES.map((r) => (
                      <option key={r} value={r}>
                        {r}
                      </option>
                    ))}
                  </select>
                </td>
                <td className="px-4 py-3 text-sm">
                  <span className={u.status === "ACTIVE" ? "text-green-700" : "text-gray-400"}>{u.status}</span>
                </td>
                <td className="px-4 py-3">
                  <div className="flex gap-3">
                    <button
                      onClick={() => updateUser(u, { status: u.status === "ACTIVE" ? "SUSPENDED" : "ACTIVE" })}
                      className="text-xs text-brand-red hover:underline"
                    >
                      {u.status === "ACTIVE" ? "Suspend" : "Reactivate"}
                    </button>
                    <button onClick={() => resetPassword(u)} className="text-xs text-brand-green hover:underline">
                      Reset password
                    </button>
                  </div>
                </td>
              </tr>
            ))}
            {users?.length === 0 && (
              <tr>
                <td colSpan={5} className="px-4 py-6 text-center text-gray-500">
                  No users in this organization yet.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}

function ManageOrganizationContent() {
  const { isPlatformAdmin, loading: authLoading } = useAuth();
  const searchParams = useSearchParams();
  const idParam = searchParams.get("id");
  const organizationId = idParam ? Number(idParam) : null;

  const [org, setOrg] = useState<Organization | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!isPlatformAdmin || organizationId === null) return;
    api
      .get<Organization>(`/api/v1/platform/organizations/${organizationId}`)
      .then(setOrg)
      .catch((err) => setError(err instanceof ApiError ? err.message : "Failed to load organization."));
  }, [isPlatformAdmin, organizationId]);

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

  if (organizationId === null) {
    return <p className="text-red-600">No organization specified.</p>;
  }

  return (
    <div>
      <Link
        href="/dashboard/platform/organizations"
        className="inline-block text-sm text-gray-500 hover:text-brand-red mb-4"
      >
        &larr; Back to all organizations
      </Link>
      <h1 className="text-2xl font-bold text-gray-800 mb-1">{org?.name ?? "Manage organization"}</h1>
      <p className="text-gray-500 mb-6">Edit this customer&apos;s organization and manage its users.</p>

      {error && <p className="text-red-600 mb-4">{error}</p>}
      {!org && !error && <p className="text-gray-500">Loading organization…</p>}

      {org && (
        <>
          <OrganizationDetailsForm org={org} onUpdated={setOrg} />
          <h2 className="text-lg font-semibold text-gray-800 mb-3">Users</h2>
          <OrganizationUsers organizationId={org.id} />
        </>
      )}
    </div>
  );
}

export default function ManageOrganizationPage() {
  return (
    <Suspense fallback={<p className="text-gray-500">Loading…</p>}>
      <ManageOrganizationContent />
    </Suspense>
  );
}
