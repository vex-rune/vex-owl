import 'package:flutter/material.dart';
import '../core/theme/app_theme.dart';
import '../core/utils/storage.dart';
import 'pages/auth/auth_page.dart';
import 'pages/chat/chat_page.dart';

class VexOwlApp extends StatelessWidget {
  const VexOwlApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Vex-Owl',
      debugShowCheckedModeBanner: false,
      theme: AppTheme.light,
      home: Storage.isLoggedIn ? const ChatPage() : const AuthPage(),
    );
  }
}
