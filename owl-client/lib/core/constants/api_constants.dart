import 'dart:io' show Platform;
import 'package:flutter/foundation.dart' show kIsWeb;

class ApiConstants {
  ApiConstants._();

  static String get baseUrl {
    if (kIsWeb) return 'http://localhost:9201';
    if (Platform.isAndroid) return 'http://10.0.2.2:9201';
    return 'http://localhost:9201';
  }

  static String get aiBaseUrl {
    if (kIsWeb) return 'http://localhost:9201';
    if (Platform.isAndroid) return 'http://10.0.2.2:9201';
    return 'http://localhost:9201';
  }

  static const String authLogin = '/api/user/auth/login';
  static const String authRegister = '/api/user/auth/register';
  static const String sendRegisterCode = '/api/user/auth/send/register/code';
  static const String sendLoginCode = '/api/user/auth/send/login/code';
  static const String freeChat = '/api/ai/chat/free';
  static const String sessionList = '/api/ai/chat/session/list';
  static String sessionMessages(String sessionId) => '/api/ai/chat/session/$sessionId/messages';
}
