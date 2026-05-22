import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../core/utils/storage_utils.dart';
import '../presentation/pages/login/login_page.dart';
import '../presentation/pages/home/home_page.dart';
import '../presentation/pages/user/user_list_page.dart';
import '../presentation/pages/user/account_list_page.dart';
import '../presentation/pages/user/user_detail_page.dart';
import '../presentation/pages/user/login_log_page.dart';
import '../presentation/pages/template/template_list_page.dart';
import '../presentation/pages/template/template_edit_page.dart';
import '../presentation/pages/template/template_preview_page.dart';

final routerProvider = Provider<GoRouter>((ref) {
  return GoRouter(
    initialLocation: '/login',
    redirect: (context, state) async {
      final isLoggedIn = await ref.read(isLoggedInProvider.future);
      final isLoginRoute = state.matchedLocation == '/login';

      if (!isLoggedIn && !isLoginRoute) {
        return '/login';
      }
      if (isLoggedIn && isLoginRoute) {
        return '/';
      }
      return null;
    },
    routes: [
      GoRoute(
        path: '/login',
        builder: (context, state) => const LoginPage(),
      ),
      ShellRoute(
        builder: (context, state, child) => HomePage(child: child),
        routes: [
          GoRoute(
            path: '/',
            redirect: (context, state) => '/users',
          ),
          GoRoute(
            path: '/users',
            builder: (context, state) => const UserListPage(),
          ),
          GoRoute(
            path: '/users/accounts',
            builder: (context, state) => const AccountListPage(),
          ),
          GoRoute(
            path: '/users/detail/:subjectId',
            builder: (context, state) {
              final subjectId = state.pathParameters['subjectId']!;
              return UserDetailPage(subjectId: subjectId);
            },
          ),
          GoRoute(
            path: '/users/login-logs',
            builder: (context, state) => const LoginLogPage(),
          ),
          GoRoute(
            path: '/templates',
            builder: (context, state) => const TemplateListPage(),
          ),
          GoRoute(
            path: '/templates/edit/:id',
            builder: (context, state) {
              final id = state.pathParameters['id'];
              return TemplateEditPage(templateId: id);
            },
          ),
          GoRoute(
            path: '/templates/preview/:id',
            builder: (context, state) {
              final id = state.pathParameters['id']!;
              return TemplatePreviewPage(templateId: id);
            },
          ),
        ],
      ),
    ],
    errorBuilder: (context, state) => Scaffold(
      body: Center(
        child: Text('Page not found: ${state.error}'),
      ),
    ),
  );
});