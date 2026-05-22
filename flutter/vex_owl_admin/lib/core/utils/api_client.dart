import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../constants/app_constants.dart';
import 'storage_utils.dart';

final dioProvider = Provider<Dio>((ref) {
  final dio = Dio(
    BaseOptions(
      baseUrl: AppConstants.apiBaseUrl,
      connectTimeout: AppConstants.httpTimeout,
      receiveTimeout: AppConstants.httpTimeout,
      headers: {
        'Content-Type': 'application/json',
      },
    ),
  );

  dio.interceptors.add(
    InterceptorsWrapper(
      onRequest: (options, handler) async {
        final token = await StorageUtils.getToken();
        if (token != null && token.isNotEmpty) {
          options.headers['Authorization'] = 'Bearer $token';
        }
        return handler.next(options);
      },
      onError: (error, handler) {
        if (error.response?.statusCode == 401) {
          StorageUtils.clearAuth();
        }
        return handler.next(error);
      },
    ),
  );

  return dio;
});

class ApiException implements Exception {
  final String message;
  final int? statusCode;

  ApiException(this.message, [this.statusCode]);

  @override
  String toString() => message;
}

class ApiClient {
  final Dio _dio;

  ApiClient(this._dio);

  Future<T> get<T>(
    String path, {
    Map<String, dynamic>? queryParameters,
    T Function(dynamic)? parser,
  }) async {
    try {
      final response = await _dio.get(path, queryParameters: queryParameters);
      final data = response.data;
      if (data['code'] == 0 || data['success'] == true) {
        final result = parser != null ? parser(data['data']) : data['data'] as T;
        return result;
      } else {
        throw ApiException(data['message'] ?? '请求失败');
      }
    } on DioException catch (e) {
      throw ApiException(
        e.response?.data?['message'] ?? e.message ?? '网络错误',
        e.response?.statusCode,
      );
    }
  }

  Future<T> post<T>(
    String path, {
    dynamic data,
    T Function(dynamic)? parser,
  }) async {
    try {
      final response = await _dio.post(path, data: data);
      final responseData = response.data;
      if (responseData['code'] == 0 || responseData['success'] == true) {
        final result = parser != null ? parser(responseData['data']) : responseData['data'] as T;
        return result;
      } else {
        throw ApiException(responseData['message'] ?? '请求失败');
      }
    } on DioException catch (e) {
      throw ApiException(
        e.response?.data?['message'] ?? e.message ?? '网络错误',
        e.response?.statusCode,
      );
    }
  }

  Future<T> put<T>(
    String path, {
    dynamic data,
    T Function(dynamic)? parser,
  }) async {
    try {
      final response = await _dio.put(path, data: data);
      final responseData = response.data;
      if (responseData['code'] == 0 || responseData['success'] == true) {
        final result = parser != null ? parser(responseData['data']) : responseData['data'] as T;
        return result;
      } else {
        throw ApiException(responseData['message'] ?? '请求失败');
      }
    } on DioException catch (e) {
      throw ApiException(
        e.response?.data?['message'] ?? e.message ?? '网络错误',
        e.response?.statusCode,
      );
    }
  }

  Future<void> delete(String path) async {
    try {
      final response = await _dio.delete(path);
      final data = response.data;
      if (data['code'] != 0 && data['success'] != true) {
        throw ApiException(data['message'] ?? '请求失败');
      }
    } on DioException catch (e) {
      throw ApiException(
        e.response?.data?['message'] ?? e.message ?? '网络错误',
        e.response?.statusCode,
      );
    }
  }
}

final apiClientProvider = Provider<ApiClient>((ref) {
  return ApiClient(ref.read(dioProvider));
});