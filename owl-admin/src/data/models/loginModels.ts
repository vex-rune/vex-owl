export interface LoginRequest {
  principal: string;
  credentials: string;
  loginType?: string;
}

export interface TokenResponse {
  token: string;
  subjectId: string;
  expiresAt?: string;
}