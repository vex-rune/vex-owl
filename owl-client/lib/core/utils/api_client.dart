import 'dart:async';
import 'dart:convert';
import 'package:http/http.dart' as http;
import '../constants/api_constants.dart';
import '../constants/app_constants.dart';
import 'storage.dart';

class ApiException implements Exception {
  final String message;
  final int? statusCode;
  ApiException(this.message, [this.statusCode]);
  @override
  String toString() => message;
}

class ApiClient {
  ApiClient._();
  static final ApiClient instance = ApiClient._();

  static bool _isSuccess(Map<String, dynamic> data) {
    final code = data['code'];
    return code == 0 || code == 'success' || data['success'] == true;
  }

  Future<Map<String, dynamic>> post(String path, {Map<String, dynamic>? body, String? baseUrl}) async {
    final url = Uri.parse('${baseUrl ?? ApiConstants.baseUrl}$path');
    final token = Storage.getToken();

    final headers = <String, String>{
      'Content-Type': 'application/json',
      if (token != null) 'Authorization': 'Bearer $token',
    };

    final response = await http.post(url, headers: headers, body: body != null ? jsonEncode(body) : null)
        .timeout(Duration(milliseconds: AppConstants.httpTimeout));

    if (response.statusCode == 401) {
      await Storage.clearAuth();
      throw ApiException('登录已过期，请重新登录', 401);
    }

    final data = jsonDecode(response.body) as Map<String, dynamic>;
    if (_isSuccess(data)) {
      return data['data'] as Map<String, dynamic>;
    }
    throw ApiException(data['message']?.toString() ?? '请求失败', response.statusCode);
  }

  Future<List<dynamic>> postList(String path, {Map<String, dynamic>? body, String? baseUrl}) async {
    final url = Uri.parse('${baseUrl ?? ApiConstants.baseUrl}$path');
    final token = Storage.getToken();

    final headers = <String, String>{
      'Content-Type': 'application/json',
      if (token != null) 'Authorization': 'Bearer $token',
    };

    final response = await http.post(url, headers: headers, body: body != null ? jsonEncode(body) : null)
        .timeout(Duration(milliseconds: AppConstants.httpTimeout));

    if (response.statusCode == 401) {
      await Storage.clearAuth();
      throw ApiException('登录已过期，请重新登录', 401);
    }

    final data = jsonDecode(response.body) as Map<String, dynamic>;
    if (_isSuccess(data)) {
      return (data['data'] as List<dynamic>?) ?? [];
    }
    throw ApiException(data['message']?.toString() ?? '请求失败', response.statusCode);
  }

  Future<List<dynamic>> getList(String path, {String? baseUrl}) async {
    final url = Uri.parse('${baseUrl ?? ApiConstants.baseUrl}$path');
    final token = Storage.getToken();

    final headers = <String, String>{
      if (token != null) 'Authorization': 'Bearer $token',
    };

    final response = await http.get(url, headers: headers)
        .timeout(Duration(milliseconds: AppConstants.httpTimeout));

    if (response.statusCode == 401) {
      await Storage.clearAuth();
      throw ApiException('登录已过期，请重新登录', 401);
    }

    final data = jsonDecode(response.body) as Map<String, dynamic>;
    if (_isSuccess(data)) {
      return (data['data'] as List<dynamic>?) ?? [];
    }
    throw ApiException(data['message']?.toString() ?? '请求失败', response.statusCode);
  }

  Stream<String> postStream(String path, {Map<String, dynamic>? body}) async* {
    final url = Uri.parse('${ApiConstants.aiBaseUrl}$path');
    final token = Storage.getToken();

    final headers = <String, String>{
      'Content-Type': 'application/json',
      'Accept': 'text/event-stream',
      if (token != null) 'Authorization': 'Bearer $token',
    };

    final request = http.Request('POST', url)..headers.addAll(headers)..body = jsonEncode(body ?? {});

    final response = await http.Client().send(request).timeout(const Duration(seconds: 60));

    if (response.statusCode == 401) {
      await Storage.clearAuth();
      throw ApiException('登录已过期，请重新登录', 401);
    }

    if (response.statusCode != 200) {
      throw ApiException('请求失败: ${response.statusCode}', response.statusCode);
    }

    final stream = response.stream.transform(utf8.decoder).transform(const LineSplitter());

    await for (final line in stream) {
      final trimmed = line.trim();
      if (trimmed.isEmpty) continue;
      if (trimmed.startsWith('data:')) {
        final data = trimmed.substring(5).trim();
        if (data == '[DONE]' || data == 'done') continue;
        if (data.isNotEmpty) yield data;
      } else if (trimmed == '[done]') {
        continue;
      } else if (trimmed.startsWith(':')) {
        continue;
      } else {
        yield trimmed;
      }
    }
  }
}
