import 'dart:convert';
import 'package:shared_preferences/shared_preferences.dart';
import '../constants/app_constants.dart';

class Storage {
  static SharedPreferences? _prefs;

  static Future<void> init() async {
    _prefs = await SharedPreferences.getInstance();
  }

  static SharedPreferences get prefs => _prefs!;

  static Future<void> setToken(String token) async {
    await prefs.setString(AppConstants.tokenKey, token);
  }

  static String? getToken() => prefs.getString(AppConstants.tokenKey);

  static Future<void> setRefreshToken(String token) async {
    await prefs.setString(AppConstants.refreshTokenKey, token);
  }

  static String? getRefreshToken() => prefs.getString(AppConstants.refreshTokenKey);

  static Future<void> setUserInfo(Map<String, String> info) async {
    await prefs.setString(AppConstants.userInfoKey, jsonEncode(info));
  }

  static Map<String, String>? getUserInfo() {
    final raw = prefs.getString(AppConstants.userInfoKey);
    if (raw == null) return null;
    try {
      return Map<String, String>.from(jsonDecode(raw));
    } catch (_) {
      return null;
    }
  }

  static Future<void> clearAuth() async {
    await prefs.remove(AppConstants.tokenKey);
    await prefs.remove(AppConstants.refreshTokenKey);
    await prefs.remove(AppConstants.userInfoKey);
  }

  static bool get isLoggedIn => getToken()?.isNotEmpty == true;
}
