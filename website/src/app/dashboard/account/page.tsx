"use client";

import { useEffect, useState, FormEvent } from "react";
import { api, ApiError } from "@/lib/api";
import { useAuth } from "@/context/AuthContext";
import { Me } from "@/lib/types";

export default function AccountPage() {
  const { me } = useAuth();
  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  const [phone, setPhone] = useState("");
  const [phoneError, setPhoneError] = useState<string | null>(null);
  const [phoneSaved, setPhoneSaved] = useState(false);
  const [phoneSubmitting, setPhoneSubmitting] = useState(false);
  const [savedPhone, setSavedPhone] = useState<string | null>(null);

  useEffect(() => {
    if (me) {
      setPhone(me.phone ?? "");
      setSavedPhone(me.phone ?? null);
    }
  }, [me]);

  const handlePhoneSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setPhoneError(null);
    setPhoneSaved(false);
    setPhoneSubmitting(true);
    try {
      const updated = await api.put<Me>("/api/v1/me/phone", { phone: phone || null });
      setSavedPhone(updated.phone);
      setPhoneSaved(true);
    } catch (err) {
      setPhoneError(err instanceof ApiError ? err.message : "Failed to update phone number.");
    } finally {
      setPhoneSubmitting(false);
    }
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    setSuccess(false);

    if (newPassword !== confirmPassword) {
      setError("New password and confirmation don't match.");
      return;
    }
    if (newPassword.length < 8) {
      setError("New password must be at least 8 characters.");
      return;
    }

    setSubmitting(true);
    try {
      await api.put("/api/v1/me/password", { currentPassword, newPassword });
      setSuccess(true);
      setCurrentPassword("");
      setNewPassword("");
      setConfirmPassword("");
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Failed to change password.");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div>
      <h1 className="text-2xl font-bold text-gray-800 mb-1">Account</h1>
      <p className="text-gray-500 mb-6">Manage your own sign-in details.</p>

      <div className="bg-white rounded-lg shadow p-5 mb-6 max-w-lg">
        <h2 className="text-lg font-semibold text-gray-800 mb-1">Signed in as</h2>
        <p className="text-sm text-gray-500">{me?.name}</p>
        <p className="text-sm text-gray-500">{me?.email}</p>
      </div>

      <div className="bg-white rounded-lg shadow p-5 mb-6 max-w-lg">
        <h2 className="text-lg font-semibold text-gray-800 mb-1">WhatsApp notifications</h2>
        <p className="text-sm text-gray-500 mb-4">
          Alerts for this organization go to this number over WhatsApp. Leave it blank to stop receiving them.
        </p>
        {phoneError && <p className="text-red-600 text-sm mb-3">{phoneError}</p>}
        {phoneSaved && (
          <p className="text-green-700 text-sm mb-3">
            {savedPhone ? `Saved — alerts will go to ${savedPhone}.` : "Saved — WhatsApp alerts are now off."}
          </p>
        )}
        <form onSubmit={handlePhoneSubmit} className="flex gap-2">
          <input
            value={phone}
            onChange={(e) => setPhone(e.target.value)}
            placeholder="+91XXXXXXXXXX"
            className="flex-1 px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-brand-red"
          />
          <button
            type="submit"
            disabled={phoneSubmitting}
            className="px-4 py-2 bg-brand-red hover:bg-brand-red-dark disabled:opacity-60 text-white text-sm font-medium rounded-lg"
          >
            {phoneSubmitting ? "Saving…" : "Save"}
          </button>
        </form>
      </div>

      <div className="bg-white rounded-lg shadow p-5 max-w-lg">
        <h2 className="text-lg font-semibold text-gray-800 mb-4">Change password</h2>
        {error && <p className="text-red-600 text-sm mb-3">{error}</p>}
        {success && <p className="text-green-700 text-sm mb-3">Password changed.</p>}
        <form onSubmit={handleSubmit} className="space-y-3">
          <div>
            <label className="block text-sm text-gray-700 mb-1">Current password</label>
            <input
              type="password"
              value={currentPassword}
              onChange={(e) => setCurrentPassword(e.target.value)}
              required
              className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-brand-red"
            />
          </div>
          <div>
            <label className="block text-sm text-gray-700 mb-1">New password</label>
            <input
              type="password"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              required
              minLength={8}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-brand-red"
            />
          </div>
          <div>
            <label className="block text-sm text-gray-700 mb-1">Confirm new password</label>
            <input
              type="password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              required
              minLength={8}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-brand-red"
            />
          </div>
          <button
            type="submit"
            disabled={submitting}
            className="px-4 py-2 bg-brand-red hover:bg-brand-red-dark disabled:opacity-60 text-white text-sm font-medium rounded-lg"
          >
            {submitting ? "Saving…" : "Change password"}
          </button>
        </form>
      </div>
    </div>
  );
}
