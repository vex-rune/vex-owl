class LoginRequest {
  final String principal;
  final String credentials;
  final String loginType;

  const LoginRequest({required this.principal, required this.credentials, required this.loginType});

  Map<String, dynamic> toJson() => {'principal': principal, 'credentials': credentials, 'loginType': loginType};
}

class RegisterRequest {
  final String email;
  final String code;
  final String password;
  final String nickname;

  const RegisterRequest({required this.email, required this.code, required this.password, required this.nickname});

  Map<String, dynamic> toJson() => {'email': email, 'code': code, 'password': password, 'nickname': nickname};
}

class VexToken {
  final String accessToken;
  final String refreshToken;
  final int expiresIn;
  final String email;
  final String nickname;
  final String role;

  const VexToken({
    required this.accessToken,
    required this.refreshToken,
    required this.expiresIn,
    required this.email,
    required this.nickname,
    required this.role,
  });

  factory VexToken.fromJson(Map<String, dynamic> json) => VexToken(
        accessToken: json['accessToken']?.toString() ?? '',
        refreshToken: json['refreshToken']?.toString() ?? '',
        expiresIn: json['expiresIn'] is int ? json['expiresIn'] : 3600,
        email: json['email']?.toString() ?? '',
        nickname: json['nickname']?.toString() ?? '',
        role: json['role']?.toString() ?? '',
      );
}

class UserInfo {
  final String email;
  final String nickname;
  final String role;

  const UserInfo({required this.email, required this.nickname, required this.role});

  Map<String, String> toJson() => {'email': email, 'nickname': nickname, 'role': role};

  factory UserInfo.fromJson(Map<String, String> json) => UserInfo(
        email: json['email'] ?? '',
        nickname: json['nickname'] ?? '',
        role: json['role'] ?? '',
      );
}
