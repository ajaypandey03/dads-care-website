"use client";

import { createContext, useContext, useEffect, useState, useCallback, ReactNode } from "react";
import { useRouter } from "next/navigation";
import { api, getToken, setToken, clearToken, setUnauthorizedHandler } from "@/lib/api";
import { LoginResponse, Me, Role } from "@/lib/types";

interface AuthUser {
  id: number;
  organizationId: number;
  role: Role;
  platformAdmin: boolean;
}

interface AuthContextValue {
  user: AuthUser | null;
  me: Me | null;
  loading: boolean;
  isPlatformAdmin: boolean;
  login: (email: string, password: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const router = useRouter();
  const [user, setUser] = useState<AuthUser | null>(null);
  const [me, setMe] = useState<Me | null>(null);
  // Always starts true (even though the static export prerenders with no window/token)
  // so server and client agree on the very first render — a lazy-initialized value here
  // would read localStorage during render and mismatch the prerendered HTML.
  const [loading, setLoading] = useState(true);

  const logout = useCallback(() => {
    clearToken();
    setUser(null);
    setMe(null);
    router.push("/login");
  }, [router]);

  useEffect(() => {
    setUnauthorizedHandler(() => logout());
    return () => setUnauthorizedHandler(null);
  }, [logout]);

  useEffect(() => {
    const token = getToken();
    if (!token) {
      // No async gap needed, but setState still can't run synchronously inside the
      // effect body (react-hooks/set-state-in-effect) — defer to a microtask instead.
      queueMicrotask(() => setLoading(false));
      return;
    }
    api
      .get<Me>("/api/v1/me")
      .then((profile) => {
        setMe(profile);
        setUser({
          id: profile.id,
          organizationId: profile.organizationId,
          role: profile.role,
          platformAdmin: profile.platformAdmin,
        });
      })
      .catch(() => {
        clearToken();
      })
      .finally(() => setLoading(false));
  }, []);

  const login = useCallback(async (email: string, password: string) => {
    const response = await api.post<LoginResponse>("/api/v1/auth/login", { email, password });
    setToken(response.accessToken);
    setUser({
      id: response.userId,
      organizationId: response.organizationId,
      role: response.role,
      platformAdmin: response.platformAdmin,
    });
    const profile = await api.get<Me>("/api/v1/me");
    setMe(profile);
  }, []);

  return (
    <AuthContext.Provider value={{ user, me, loading, isPlatformAdmin: user?.platformAdmin ?? false, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
}
