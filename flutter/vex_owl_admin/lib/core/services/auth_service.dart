import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../../core/constants/app_constants.dart';
import '../../core/constants/api_constants.dart';
import '../../core/utils/api_client.dart';
import '../../core/utils/storage_utils.dart';
import '../../data/models/login_models.dart';

final authServiceProvider = Provider<AuthService>((ref) {
  return AuthService(ref.read(apiClientProvider));
});

class AuthService {
  final ApiClient _apiClient;

  AuthService(this._apiClient);

  Future<TokenResponse> login(LoginRequest request) async {
    final response = await _apiClient.post<TokenResponse>(
      ApiConstants.authLogin,
      data: request.toJson(),
      parser: (data) => TokenResponse.fromJson(data),
    );
    final token = response.token;
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(AppConstants.tokenKey, token);
    await prefs.setString(AppConstants.userKey, response.subjectId);
    return response;
  }

  Future<void> logout() async {
    await StorageUtils.clearAuth();
  }

  Future<bool> isLoggedIn() async {
    final prefs = await SharedPreferences.getInstance();
    final token = prefs.getString(AppConstants.tokenKey);
    return token != null && token.isNotEmpty;
  }
}