"use client";

import { useEffect, useState } from "react";
import { api, ApiError } from "@/lib/api";
import { Alert, AlertClassification } from "@/lib/types";

const CLASSIFICATION_STYLE: Record<AlertClassification, string> = {
  CONFIRMED: "bg-green-100 text-green-700",
  UNEXPLAINED_HIGH: "bg-red-100 text-red-700",
  UNEXPLAINED_VERIFY: "bg-amber-100 text-amber-700",
  SUPPRESSED: "bg-gray-100 text-gray-600",
};

const CLASSIFICATION_LABEL: Record<AlertClassification, string> = {
  CONFIRMED: "Confirmed Shutter Open",
  UNEXPLAINED_HIGH: "Unexplained Access — High",
  UNEXPLAINED_VERIFY: "Unexplained Access — Verify",
  SUPPRESSED: "Suppressed",
};

function AlertRow({ alert, onFeedback }: { alert: Alert; onFeedback: (id: number, wasCorrect: boolean) => void }) {
  const [submitted, setSubmitted] = useState(false);

  return (
    <tr className="border-t border-gray-100">
      <td className="px-4 py-3 font-mono text-sm text-gray-700">{alert.sequenceCode ?? "—"}</td>
      <td className="px-4 py-3 text-sm text-gray-500">{new Date(alert.createdAt).toLocaleString()}</td>
      <td className="px-4 py-3 text-sm text-gray-700">Device #{alert.deviceId}</td>
      <td className="px-4 py-3 text-sm text-gray-700">{alert.direction}</td>
      <td className="px-4 py-3">
        <span className={`px-2.5 py-1 rounded-full text-xs font-semibold ${CLASSIFICATION_STYLE[alert.classification]}`}>
          {CLASSIFICATION_LABEL[alert.classification]}
        </span>
      </td>
      <td className="px-4 py-3 text-sm text-gray-500">{alert.confidenceScore ?? "—"}</td>
      <td className="px-4 py-3">
        {submitted ? (
          <span className="text-sm text-gray-400">Thanks!</span>
        ) : (
          <div className="flex gap-2">
            <button
              onClick={() => {
                onFeedback(alert.id, true);
                setSubmitted(true);
              }}
              className="text-xs px-2 py-1 rounded bg-green-50 text-green-700 hover:bg-green-100"
            >
              Correct
            </button>
            <button
              onClick={() => {
                onFeedback(alert.id, false);
                setSubmitted(true);
              }}
              className="text-xs px-2 py-1 rounded bg-red-50 text-red-700 hover:bg-red-100"
            >
              Not correct
            </button>
          </div>
        )}
      </td>
    </tr>
  );
}

export default function AlertsPage() {
  const [alerts, setAlerts] = useState<Alert[] | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    api
      .get<Alert[]>("/api/v1/alerts")
      .then(setAlerts)
      .catch((err) => setError(err instanceof ApiError ? err.message : "Failed to load alerts."));
  }, []);

  const submitFeedback = async (id: number, wasCorrect: boolean) => {
    try {
      await api.post(`/api/v1/alerts/${id}/feedback`, { wasCorrect });
    } catch {
      // Best-effort — the row already shows "Thanks!" optimistically; a failed feedback
      // submission isn't worth blocking the operator's flow over.
    }
  };

  return (
    <div>
      <h1 className="text-2xl font-bold text-gray-800 mb-1">Alerts</h1>
      <p className="text-gray-500 mb-6">
        Every lock event, classified by whether it matched a command your app actually sent. See{" "}
        <span className="font-medium">Confirmed Shutter Open</span> vs.{" "}
        <span className="font-medium">Unexplained Access</span>.
      </p>

      {error && <p className="text-red-600 mb-4">{error}</p>}
      {alerts === null && !error && <p className="text-gray-500">Loading alerts…</p>}
      {alerts?.length === 0 && <p className="text-gray-500">No alerts yet.</p>}

      {alerts && alerts.length > 0 && (
        <div className="bg-white rounded-lg shadow overflow-x-auto">
          <table className="w-full text-left">
            <thead className="bg-gray-50 text-xs uppercase text-gray-500">
              <tr>
                <th className="px-4 py-3">Ref</th>
                <th className="px-4 py-3">When</th>
                <th className="px-4 py-3">Device</th>
                <th className="px-4 py-3">Direction</th>
                <th className="px-4 py-3">Classification</th>
                <th className="px-4 py-3">Score</th>
                <th className="px-4 py-3">Was this correct?</th>
              </tr>
            </thead>
            <tbody>
              {alerts.map((alert) => (
                <AlertRow key={alert.id} alert={alert} onFeedback={submitFeedback} />
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
