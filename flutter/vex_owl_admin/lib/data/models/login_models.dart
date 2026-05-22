class LoginRequest {
  final String principal;
  final String credentials;
  final String loginType;

  LoginRequest({
    required this.principal,
    required this.credentials,
    this.loginType = 'admin',
  });

  Map<String, dynamic> toJson() => {
        'principal': principal,
        'credentials': credentials,
        'loginType': loginType,
      };
}

class TokenResponse {
  final String token;
  final String subjectId;
  final DateTime? expiresAt;

  TokenResponse({
    required this.token,
    required this.subjectId,
    this.expiresAt,
  });

  factory TokenResponse.fromJson(Map<String, dynamic> json) {
    return TokenResponse(
      token: json['token'] ?? '',
      subjectId: json['subjectId'] ?? '',
      expiresAt: json['expiresAt'] != null
          ? DateTime.tryParse(json['expiresAt'].toString())
          : null,
    );
  }
}