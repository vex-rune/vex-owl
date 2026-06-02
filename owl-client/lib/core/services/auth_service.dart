import '../constants/api_constants.dart';
import '../utils/api_client.dart';
import '../utils/storage.dart';
import '../../data/models/auth_models.dart';

class AuthService {
  final _client = ApiClient.instance;

  Future<VexToken> login(LoginRequest request) async {
    final json = await _client.post(ApiConstants.authLogin, body: request.toJson());
    final token = VexToken.fromJson(json);
    await _saveAuth(token);
    return token;
  }

  Future<VexToken> register(RegisterRequest request) async {
    final json = await _client.post(ApiConstants.authRegister, body: request.toJson());
    final token = VexToken.fromJson(json);
    await _saveAuth(token);
    return token;
  }

  Future<void> sendRegisterCode(String email) async {
    await _client.post(ApiConstants.sendRegisterCode, body: {'email': email});
  }

  Future<void> sendLoginCode(String email) async {
    await _client.post(ApiConstants.sendLoginCode, body: {'email': email});
  }

  Future<void> logout() async {
    await Storage.clearAuth();
  }

  Future<void> _saveAuth(VexToken token) async {
    await Storage.setToken(token.accessToken);
    await Storage.setRefreshToken(token.refreshToken);
    await Storage.setUserInfo(UserInfo(email: token.email, nickname: token.nickname, role: token.role).toJson());
  }
}
