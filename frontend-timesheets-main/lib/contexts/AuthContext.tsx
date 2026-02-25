'use client';

import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { authService, type UserInfo, type AuthResponse } from '@/lib/services/auth.service';

interface AuthContextType {
  isAuthenticated: boolean;
  user: UserInfo | null;
  loading: boolean;
  login: (username: string, password: string) => Promise<AuthResponse>;
  logout: () => Promise<void>;
  refreshUserInfo: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [isAuthenticated, setIsAuthenticated] = useState<boolean>(false);
  const [user, setUser] = useState<UserInfo | null>(null);
  const [loading, setLoading] = useState<boolean>(true);

  const refreshUserInfo = async () => {
    if (authService.isAuthenticated()) {
      try {
        const userInfo = await authService.getUserInfo();
        console.log('🔍 AuthContext: Refreshed userInfo:', userInfo);
        if (!userInfo.role || userInfo.role.trim() === '') {
          console.error('❌ AuthContext: CRITICAL - User info has no role!', userInfo);
          console.error('❌ This will cause "User role missing from session" error');
        }
        setUser(userInfo);
        setIsAuthenticated(true);
      } catch (error) {
        console.error('Failed to get user info:', error);
        setIsAuthenticated(false);
        setUser(null);
        if (typeof window !== 'undefined') {
          localStorage.removeItem('authToken');
        }
      }
    } else {
      setIsAuthenticated(false);
      setUser(null);
    }
    setLoading(false);
  };

  const login = async (username: string, password: string) => {
    setLoading(true);
    try {
      const response = await authService.login({ username, password });
      console.log('🔍 AuthContext: Login response user:', response.user);
      if (!response.user.role || response.user.role.trim() === '') {
        console.error('❌ AuthContext: CRITICAL - Login response has no role!', response.user);
        console.error('❌ This will cause "User role missing from session" error');
      }
      setUser(response.user);
      setIsAuthenticated(true);
      return response; // Return response so LoginCard can access user role
    } catch (error) {
      setIsAuthenticated(false);
      setUser(null);
      throw error;
    } finally {
      setLoading(false);
    }
  };

  const logout = async () => {
    setLoading(true);
    try {
      await authService.logout();
    } catch (error) {
      console.error('Logout error:', error);
    } finally {
      setIsAuthenticated(false);
      setUser(null);
      setLoading(false);
    }
  };

  // Check authentication on mount
  useEffect(() => {
    refreshUserInfo();
  }, []);

  return (
    <AuthContext.Provider
      value={{
        isAuthenticated,
        user,
        loading,
        login,
        logout,
        refreshUserInfo,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}

