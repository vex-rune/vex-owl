import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../core/constants/api_constants.dart';
import '../../core/utils/api_client.dart';
import '../../data/models/user_models.dart';
import '../../data/models/query_models.dart';

final userServiceProvider = Provider<UserService>((ref) {
  return UserService(ref.read(apiClientProvider));
});

class UserService {
  final ApiClient _apiClient;

  UserService(this._apiClient);

  Future<List<SubjectEntity>> querySubjects(QueriesPageRequest request) async {
    final response = await _apiClient.post<List<SubjectEntity>>(
      ApiConstants.subjectQuery,
      data: request.toJson(),
      parser: (data) => (data as List).map((e) => SubjectEntity.fromJson(e)).toList(),
    );
    return response;
  }

  Future<List<AccountEntity>> queryAccounts(QueriesPageRequest request) async {
    final response = await _apiClient.post<List<AccountEntity>>(
      ApiConstants.accountQuery,
      data: request.toJson(),
      parser: (data) => (data as List).map((e) => AccountEntity.fromJson(e)).toList(),
    );
    return response;
  }

  Future<List<UserProfileEntity>> queryUsers(QueriesPageRequest request) async {
    final response = await _apiClient.post<List<UserProfileEntity>>(
      ApiConstants.userQuery,
      data: request.toJson(),
      parser: (data) => (data as List).map((e) => UserProfileEntity.fromJson(e)).toList(),
    );
    return response;
  }

  Future<UserProfileEntity> getUser(String userId) async {
    final response = await _apiClient.get<UserProfileEntity>(
      '${ApiConstants.userGet}/$userId',
      parser: (data) => UserProfileEntity.fromJson(data),
    );
    return response;
  }

  Future<List<LoginRecordEntity>> queryLoginLogs(QueriesPageRequest request) async {
    final response = await _apiClient.post<List<LoginRecordEntity>>(
      ApiConstants.loginLogQuery,
      data: request.toJson(),
      parser: (data) => (data as List).map((e) => LoginRecordEntity.fromJson(e)).toList(),
    );
    return response;
  }
}