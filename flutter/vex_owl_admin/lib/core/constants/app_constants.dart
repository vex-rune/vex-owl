class AppConstants {
  AppConstants._();

  static const String appName = 'Vex-Owl 管理后台';

  static const String apiBaseUrl = 'http://localhost:9200';

  static const String tokenKey = 'auth_token';
  static const String userKey = 'auth_user';

  static const int pageSize = 20;

  static const Duration httpTimeout = Duration(seconds: 30);

  static const double sidebarWidth = 240.0;
  static const double sidebarCollapsedWidth = 72.0;

  static const double mobileBreakpoint = 600.0;
  static const double tabletBreakpoint = 1024.0;
}