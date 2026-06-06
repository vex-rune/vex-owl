import { AppConstants } from '../constants/appConstants';

class Storage {
  setToken(token: string): void {
    localStorage.setItem(AppConstants.tokenKey, token);
  }

  getToken(): string | null {
    return localStorage.getItem(AppConstants.tokenKey);
  }

  setUserId(userId: string): void {
    localStorage.setItem(AppConstants.userKey, userId);
  }

  getUserId(): string | null {
    return localStorage.getItem(AppConstants.userKey);
  }

  clearAuth(): void {
    localStorage.removeItem(AppConstants.tokenKey);
    localStorage.removeItem(AppConstants.userKey);
  }

  isLoggedIn(): boolean {
    const token = this.getToken();
    return token !== null && token.length > 0;
  }
}

export const storage = new Storage();