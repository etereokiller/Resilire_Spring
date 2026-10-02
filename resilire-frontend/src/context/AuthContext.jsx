import { createContext, useContext, useEffect, useState } from "react";
import * as authApi from "../api/authApi";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const stored = localStorage.getItem("resilire_user");
    return stored ? JSON.parse(stored) : null;
  });
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (user) {
      localStorage.setItem("resilire_user", JSON.stringify(user));
    } else {
      localStorage.removeItem("resilire_user");
    }
  }, [user]);

  const applyAuthResponse = (authResponse) => {
    localStorage.setItem("resilire_token", authResponse.token);
    const nextUser = {
      userId: authResponse.userId,
      email: authResponse.email,
      role: authResponse.role,
    };
    setUser(nextUser);
    return nextUser;
  };

  const login = async (email, password) => {
    setLoading(true);
    try {
      const response = await authApi.login(email, password);
      return applyAuthResponse(response);
    } finally {
      setLoading(false);
    }
  };

  const registerPatient = async (payload) => {
    setLoading(true);
    try {
      const response = await authApi.registerPatient(payload);
      return applyAuthResponse(response);
    } finally {
      setLoading(false);
    }
  };

  const registerDoctor = async (payload) => {
    setLoading(true);
    try {
      const response = await authApi.registerDoctor(payload);
      return applyAuthResponse(response);
    } finally {
      setLoading(false);
    }
  };

  const logout = () => {
    localStorage.removeItem("resilire_token");
    setUser(null);
  };

  return (
    <AuthContext.Provider
      value={{ user, loading, login, registerPatient, registerDoctor, logout }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
}
