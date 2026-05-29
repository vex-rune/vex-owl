import { create } from 'zustand';
import { authService } from '../core/services/authService';
import { LoginRequest } from '../data/models/loginModels';

interface LoginState {
  isLoading: boolean;
  error: string | null;
  login: (principal: string, credentials: string) => Promise<void>;
  logout: () => Promise<void>;
  clearError: () => void;
}

export const useLoginStore = create<LoginState>((set) => ({
  isLoading: false,
  error: null,
  login: async (principal: string, credentials: string) => {
    set({ isLoading: true, error: null });
    try {
      const request: LoginRequest = { principal, credentials, loginType: 'admin' };
      await authService.login(request);
      set({ isLoading: false });
    } catch (error) {
      set({ isLoading: false, error: error instanceof Error ? error.message : '登录失败，请稍后重试' });
    }
  },
  logout: async () => {
    await authService.logout();
  },
  clearError: () => set({ error: null }),
}));