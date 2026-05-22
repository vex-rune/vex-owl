import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../core/constants/api_constants.dart';
import '../../core/utils/api_client.dart';
import '../../data/models/template_models.dart';
import '../../data/models/query_models.dart';

final templateServiceProvider = Provider<TemplateService>((ref) {
  return TemplateService(ref.read(apiClientProvider));
});

class TemplateService {
  final ApiClient _apiClient;

  TemplateService(this._apiClient);

  Future<List<TemplateEntity>> queryTemplates(QueriesPageRequest request) async {
    final response = await _apiClient.post<List<TemplateEntity>>(
      ApiConstants.templateQuery,
      data: request.toJson(),
      parser: (data) => (data as List).map((e) => TemplateEntity.fromJson(e)).toList(),
    );
    return response;
  }

  Future<TemplateEntity> getTemplate(String id) async {
    final response = await _apiClient.get<TemplateEntity>(
      '${ApiConstants.template}/$id',
      parser: (data) => TemplateEntity.fromJson(data),
    );
    return response;
  }

  Future<TemplateEntity> getTemplateByCode(String code) async {
    final response = await _apiClient.get<TemplateEntity>(
      '${ApiConstants.templateByCode}/$code',
      parser: (data) => TemplateEntity.fromJson(data),
    );
    return response;
  }

  Future<TemplateEntity> createTemplate(Map<String, dynamic> data) async {
    final response = await _apiClient.post<TemplateEntity>(
      ApiConstants.template,
      data: data,
      parser: (data) => TemplateEntity.fromJson(data),
    );
    return response;
  }

  Future<TemplateEntity> updateTemplate(String id, TemplateUpdateRequest request) async {
    final response = await _apiClient.put<TemplateEntity>(
      '${ApiConstants.template}/$id',
      data: request.toJson(),
      parser: (data) => TemplateEntity.fromJson(data),
    );
    return response;
  }

  Future<void> deleteTemplate(String id) async {
    await _apiClient.delete('${ApiConstants.template}/$id');
  }
}