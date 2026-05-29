export interface SubjectEntity {
  id: string;
  name?: string;
  type?: string;
  status?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface UserProfileEntity {
  id: string;
  nickname?: string;
  avatar?: string;
  email?: string;
  phone?: string;
  status?: string;
  createdAt?: string;
}

export interface AccountEntity {
  id: string;
  subjectId: string;
  account?: string;
  type?: string;
  enabled?: boolean;
  createdAt?: string;
}

export interface LoginRecordEntity {
  id: string;
  subjectId: string;
  accountId?: string;
  loginTime?: number;
  loginType?: string;
}