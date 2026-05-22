class SubjectEntity {
  final String id;
  final String? name;
  final String? type;
  final String? status;
  final DateTime? createdAt;
  final DateTime? updatedAt;

  SubjectEntity({
    required this.id,
    this.name,
    this.type,
    this.status,
    this.createdAt,
    this.updatedAt,
  });

  factory SubjectEntity.fromJson(Map<String, dynamic> json) {
    return SubjectEntity(
      id: json['id'] ?? '',
      name: json['name'],
      type: json['type'],
      status: json['status'],
      createdAt: json['createdAt'] != null
          ? DateTime.tryParse(json['createdAt'].toString())
          : null,
      updatedAt: json['updatedAt'] != null
          ? DateTime.tryParse(json['updatedAt'].toString())
          : null,
    );
  }
}

class UserProfileEntity {
  final String id;
  final String? nickname;
  final String? avatar;
  final String? email;
  final String? phone;
  final String? status;
  final DateTime? createdAt;

  UserProfileEntity({
    required this.id,
    this.nickname,
    this.avatar,
    this.email,
    this.phone,
    this.status,
    this.createdAt,
  });

  factory UserProfileEntity.fromJson(Map<String, dynamic> json) {
    return UserProfileEntity(
      id: json['id'] ?? '',
      nickname: json['nickname'],
      avatar: json['avatar'],
      email: json['email'],
      phone: json['phone'],
      status: json['status'],
      createdAt: json['createdAt'] != null
          ? DateTime.tryParse(json['createdAt'].toString())
          : null,
    );
  }
}

class AccountEntity {
  final String id;
  final String subjectId;
  final String? account;
  final String? type;
  final bool? enabled;
  final DateTime? createdAt;

  AccountEntity({
    required this.id,
    required this.subjectId,
    this.account,
    this.type,
    this.enabled,
    this.createdAt,
  });

  factory AccountEntity.fromJson(Map<String, dynamic> json) {
    return AccountEntity(
      id: json['id'] ?? '',
      subjectId: json['subjectId'] ?? '',
      account: json['account'],
      type: json['type'],
      enabled: json['enabled'],
      createdAt: json['createdAt'] != null
          ? DateTime.tryParse(json['createdAt'].toString())
          : null,
    );
  }
}

class LoginRecordEntity {
  final String id;
  final String subjectId;
  final String? accountId;
  final DateTime? loginTime;
  final String? loginType;

  LoginRecordEntity({
    required this.id,
    required this.subjectId,
    this.accountId,
    this.loginTime,
    this.loginType,
  });

  factory LoginRecordEntity.fromJson(Map<String, dynamic> json) {
    return LoginRecordEntity(
      id: json['id'] ?? '',
      subjectId: json['subjectId'] ?? '',
      accountId: json['accountId'],
      loginTime: json['loginTime'] != null
          ? DateTime.fromMillisecondsSinceEpoch(json['loginTime'])
          : null,
      loginType: json['loginType'],
    );
  }
}