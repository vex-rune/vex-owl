class TemplateEntity {
  final String id;
  final String name;
  final String code;
  final String? content;
  final String? remark;
  final bool? enabled;
  final DateTime? createdAt;
  final DateTime? updatedAt;

  TemplateEntity({
    required this.id,
    required this.name,
    required this.code,
    this.content,
    this.remark,
    this.enabled,
    this.createdAt,
    this.updatedAt,
  });

  factory TemplateEntity.fromJson(Map<String, dynamic> json) {
    return TemplateEntity(
      id: json['id'] ?? '',
      name: json['name'] ?? '',
      code: json['code'] ?? '',
      content: json['content'],
      remark: json['remark'],
      enabled: json['enabled'],
      createdAt: json['createdAt'] != null
          ? DateTime.tryParse(json['createdAt'].toString())
          : null,
      updatedAt: json['updatedAt'] != null
          ? DateTime.tryParse(json['updatedAt'].toString())
          : null,
    );
  }

  Map<String, dynamic> toUpdateJson() => {
        if (name.isNotEmpty) 'name': name,
        if (content != null) 'content': content,
        if (remark != null) 'remark': remark,
        if (enabled != null) 'enabled': enabled,
      };
}

class TemplateUpdateRequest {
  final String? name;
  final String? content;
  final String? remark;
  final bool? enabled;

  TemplateUpdateRequest({
    this.name,
    this.content,
    this.remark,
    this.enabled,
  });

  Map<String, dynamic> toJson() => {
        if (name != null) 'name': name,
        if (content != null) 'content': content,
        if (remark != null) 'remark': remark,
        if (enabled != null) 'enabled': enabled,
      };
}