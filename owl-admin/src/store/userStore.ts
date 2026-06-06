import { create } from 'zustand';
import { userService } from '../core/services/userService';
import { SubjectEntity, AccountEntity, LoginRecordEntity } from '../data/models/userModels';
import { QueriesPageRequest } from '../data/models/queryModels';

interface UserState {
  subjects: SubjectEntity[];
  accounts: AccountEntity[];
  loginLogs: LoginRecordEntity[];
  isLoading: boolean;
  error: string | null;
  fetchSubjects: () => Promise<void>;
  fetchAccounts: () => Promise<void>;
  fetchLoginLogs: () => Promise<void>;
}

export const useUserStore = create<UserState>((set) => ({
  subjects: [],
  accounts: [],
  loginLogs: [],
  isLoading: false,
  error: null,
  fetchSubjects: async () => {
    set({ isLoading: true, error: null });
    try {
      const request: QueriesPageRequest = { page: { page: 0, size: 100 } };
      const subjects = await userService.querySubjects(request);
      set({ subjects, isLoading: false });
    } catch (error) {
      set({ isLoading: false, error: error instanceof Error ? error.message : '加载失败' });
    }
  },
  fetchAccounts: async () => {
    set({ isLoading: true, error: null });
    try {
      const request: QueriesPageRequest = { page: { page: 0, size: 100 } };
      const accounts = await userService.queryAccounts(request);
      set({ accounts, isLoading: false });
    } catch (error) {
      set({ isLoading: false, error: error instanceof Error ? error.message : '加载失败' });
    }
  },
  fetchLoginLogs: async () => {
    set({ isLoading: true, error: null });
    try {
      const request: QueriesPageRequest = { page: { page: 0, size: 100 }, order: [{ field: 'loginTime', direction: 'desc' }] };
      const loginLogs = await userService.queryLoginLogs(request);
      set({ loginLogs, isLoading: false });
    } catch (error) {
      set({ isLoading: false, error: error instanceof Error ? error.message : '加载失败' });
    }
  },
}));