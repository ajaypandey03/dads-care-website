import { login as loginRequest } from "@/api/authApi";
import { loadStoredToken, setSessionExpiredHandler, setToken } from "@/api/apiClient";
import { getMe } from "@/api/meApi";
import { User } from "@/types/models";
import React, { createContext, useContext, useEffect, useState } from "react";

interface AuthContextValue {
  user: User | null;
  isLoading: boolean;
  login: (email: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    // A session-expired 401 (see apiClient) clears the stored token — mirror that here
    // so the app immediately falls back to the login screen instead of showing stale
    // "logged in" UI until the next manual action fails too.
    setSessionExpiredHandler(() => setUser(null));

    (async () => {
      const token = await loadStoredToken();
      if (token) {
        try {
          setUser(await getMe());
        } catch {
          await setToken(null);
        }
      }
      setIsLoading(false);
    })();
  }, []);

  const login = async (email: string, password: string) => {
    const response = await loginRequest(email, password);
    await setToken(response.accessToken);
    setUser(await getMe());
  };

  const logout = async () => {
    await setToken(null);
    setUser(null);
  };

  return <AuthContext.Provider value={{ user, isLoading, login, logout }}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
}
