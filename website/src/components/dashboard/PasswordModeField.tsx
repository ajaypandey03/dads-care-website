"use client";

// Shared by every admin "create a user" form (Team invite, platform org onboarding,
// platform add-user-to-org) — lets the admin either get a generated temporary password
// (shown once, relayed out-of-band) or set the initial password themselves. No email is
// sent either way — dadscare-backend has no SMTP integration yet.

export type PasswordMode = "generate" | "manual";

export function PasswordModeField({
  mode,
  onModeChange,
  password,
  onPasswordChange,
  label = "Initial password",
}: {
  mode: PasswordMode;
  onModeChange: (mode: PasswordMode) => void;
  password: string;
  onPasswordChange: (value: string) => void;
  label?: string;
}) {
  return (
    <div className="sm:col-span-2">
      <p className="text-sm font-medium text-gray-700 mb-2">{label}</p>
      <div className="flex gap-4 mb-2">
        <label className="flex items-center gap-2 text-sm text-gray-700">
          <input
            type="radio"
            checked={mode === "generate"}
            onChange={() => onModeChange("generate")}
            className="border-gray-300"
          />
          Auto-generate
        </label>
        <label className="flex items-center gap-2 text-sm text-gray-700">
          <input
            type="radio"
            checked={mode === "manual"}
            onChange={() => onModeChange("manual")}
            className="border-gray-300"
          />
          Set manually
        </label>
      </div>
      {mode === "manual" && (
        <input
          type="text"
          value={password}
          onChange={(e) => onPasswordChange(e.target.value)}
          placeholder="At least 8 characters"
          required
          minLength={8}
          className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-brand-red"
        />
      )}
      {mode === "generate" && (
        <p className="text-xs text-gray-400">
          A random password is generated and shown once after creation — no email is sent.
        </p>
      )}
    </div>
  );
}
