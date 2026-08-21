"use client";

import { useEffect, useState, FormEvent } from "react";
import { api, ApiError } from "@/lib/api";
import { CreateUserResponse, Role, UserAdmin } from "@/lib/types";
import { PasswordMode, PasswordModeField } from "@/components/dashboard/PasswordModeField";

const ROLES: Role[] = ["ORG_ADMIN", "SITE_MANAGER", "OPERATOR", "VIEWER"];

export default function TeamPage() {
  const [users, setUsers] = useState<UserAdmin[] | null>(null);
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [phone, setPhone] = useState("");
  const [role, setRole] = useState<Role>("VIEWER");
  const [passwordMode, setPasswordMode] = useState<PasswordMode>("generate");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [justCreated, setJustCreated] = useState<CreateUserResponse | null>(null);

  const load = () => api.get<UserAdmin[]>("/api/v1/users").then(setUsers);

  useEffect(() => {
    load().catch((err) => setError(err instanceof ApiError ? err.message : "Failed to load team."));
  }, []);

  const handleInvite = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    setJustCreated(null);
    setSubmitting(true);
    try {
      const response = await api.post<CreateUserResponse>("/api/v1/users", {
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
      setError(err instanceof ApiError ? err.message : "Failed to invite user.");
    } finally {
      setSubmitting(false);
    }
  };

  const updateUser = async (user: UserAdmin, patch: Partial<Pick<UserAdmin, "role" | "status">>) => {
    await api.put(`/api/v1/users/${user.id}`, {
      role: patch.role ?? user.role,
      status: patch.status ?? user.status,
    });
    await load();
  };

  return (
    <div>
      <h1 className="text-2xl font-bold text-gray-800 mb-1">Team</h1>
      <p className="text-gray-500 mb-6">Invite teammates and manage who can access this organization.</p>

      {error && <p className="text-red-600 mb-4">{error}</p>}

      {justCreated && (
        <div className="mb-6 rounded-lg bg-amber-50 border border-amber-200 px-4 py-3 text-sm text-amber-800">
          {justCreated.temporaryPassword ? (
            <>
              <p className="font-semibold mb-1">
                {justCreated.user.name} was invited — share this temporary password with them directly (it
                won&apos;t be shown again):
              </p>
              <code className="bg-white px-2 py-1 rounded border border-amber-200 text-amber-900">
                {justCreated.temporaryPassword}
              </code>
            </>
          ) : (
            <p className="font-semibold">
              {justCreated.user.name} was invited with the password you set — let them know it directly.
            </p>
          )}
        </div>
      )}

      <div className="bg-white rounded-lg shadow p-5 mb-6">
        <h2 className="text-lg font-semibold text-gray-800 mb-4">Invite a teammate</h2>
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
            {submitting ? "Inviting…" : "Invite"}
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
                  <button
                    onClick={() => updateUser(u, { status: u.status === "ACTIVE" ? "SUSPENDED" : "ACTIVE" })}
                    className="text-xs text-brand-red hover:underline"
                  >
                    {u.status === "ACTIVE" ? "Suspend" : "Reactivate"}
                  </button>
                </td>
              </tr>
            ))}
            {users?.length === 0 && (
              <tr>
                <td colSpan={5} className="px-4 py-6 text-center text-gray-500">
                  No teammates yet.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
