class ApiConstants {
  ApiConstants._();

  static const String baseUrl = 'http://localhost:9200';

  static const String authLogin = '/api/user/auth/login';

  static const String subjectQuery = '/api/user/admin/subject/query';
  static const String accountQuery = '/api/user/admin/account/query';
  static const String userQuery = '/api/user/admin/user/query';
  static const String userGet = '/api/user/admin/user';
  static const String loginLogQuery = '/api/user/admin/login/log/query';

  static const String templateQuery = '/api/notification/admin/template/query';
  static const String template = '/api/notification/admin/template';
  static const String templateByCode = '/api/notification/admin/template/code';
}