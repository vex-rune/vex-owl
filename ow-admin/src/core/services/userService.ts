import { apiClient } from '../utils/apiClient';
import { ApiConstants } from '../constants/apiConstants';
import { SubjectEntity, AccountEntity, UserProfileEntity, LoginRecordEntity } from '../../data/models/userModels';
import { QueriesPageRequest } from '../../data/models/queryModels';

export class UserService {
  async querySubjects(request: QueriesPageRequest): Promise<SubjectEntity[]> {
    return apiClient.post<SubjectEntity[]>(ApiConstants.subjectQuery, request);
  }

  async queryAccounts(request: QueriesPageRequest): Promise<AccountEntity[]> {
    return apiClient.post<AccountEntity[]>(ApiConstants.accountQuery, request);
  }

  async queryUsers(request: QueriesPageRequest): Promise<UserProfileEntity[]> {
    return apiClient.post<UserProfileEntity[]>(ApiConstants.userQuery, request);
  }

  async getUser(userId: string): Promise<UserProfileEntity> {
    return apiClient.get<UserProfileEntity>(`${ApiConstants.userGet}/${userId}`);
  }

  async queryLoginLogs(request: QueriesPageRequest): Promise<LoginRecordEntity[]> {
    return apiClient.post<LoginRecordEntity[]>(ApiConstants.loginLogQuery, request);
  }
}

export const userService = new UserService();