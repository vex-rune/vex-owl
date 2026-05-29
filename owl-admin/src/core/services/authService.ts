import { apiClient, ApiException } from '../utils/apiClient';
import { storage } from '../utils/storage';
import { ApiConstants } from '../constants/apiConstants';
import { LoginRequest, TokenResponse } from '../../data/models/loginModels';

export class AuthService {
  async login(request: LoginRequest): Promise<TokenResponse> {
    try {
      const response = await apiClient.post<TokenResponse>(ApiConstants.authLogin, {
        principal: request.principal,
        credentials: request.credentials,
        loginType: request.loginType || 'admin',
      });
      storage.setToken(response.token);
      storage.setUserId(response.subjectId);
      return response;
    } catch (error) {
      if (error instanceof ApiException) {
        throw error;
      }
      throw new ApiException('登录失败，请稍后重试');
    }
  }

  async logout(): Promise<void> {
    storage.clearAuth();
  }

  isLoggedIn(): boolean {
    return storage.isLoggedIn();
  }
}

export const authService = new AuthService();