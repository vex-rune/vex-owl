import 'dart:async';
import 'package:flutter/material.dart';
import '../../../core/services/auth_service.dart';
import '../../../core/utils/api_client.dart';
import '../../../data/models/auth_models.dart';
import '../chat/chat_page.dart';

class AuthPage extends StatefulWidget {
  const AuthPage({super.key});

  @override
  State<AuthPage> createState() => _AuthPageState();
}

class _AuthPageState extends State<AuthPage> {
  final _authService = AuthService();
  final _emailController = TextEditingController();
  final _passwordController = TextEditingController();
  final _codeController = TextEditingController();
  final _nicknameController = TextEditingController();

  bool _isLogin = true;
  bool _useCode = false;
  bool _isLoading = false;
  int _codeCountdown = 0;
  String? _error;
  String? _success;
  Timer? _timer;

  @override
  void initState() {
    super.initState();
    _emailController.addListener(() => setState(() {}));
  }

  @override
  void dispose() {
    _emailController.dispose();
    _passwordController.dispose();
    _codeController.dispose();
    _nicknameController.dispose();
    _timer?.cancel();
    super.dispose();
  }

  void _startCountdown() {
    setState(() => _codeCountdown = 60);
    _timer?.cancel();
    _timer = Timer.periodic(const Duration(seconds: 1), (timer) {
      if (_codeCountdown <= 1) {
        timer.cancel();
        setState(() => _codeCountdown = 0);
      } else {
        setState(() => _codeCountdown--);
      }
    });
  }

  void _switchMode(bool toLogin) {
    setState(() {
      _isLogin = toLogin;
      _error = null;
      _success = null;
      _emailController.clear();
      _passwordController.clear();
      _codeController.clear();
      _nicknameController.clear();
    });
  }

  Future<void> _sendCode(String type) async {
    if (_emailController.text.isEmpty || _codeCountdown > 0) return;
    setState(() {
      _error = null;
      _success = null;
    });
    try {
      if (type == 'register') {
        await _authService.sendRegisterCode(_emailController.text);
      } else {
        await _authService.sendLoginCode(_emailController.text);
      }
      if (mounted) setState(() => _success = '验证码已发送到您的邮箱');
      _startCountdown();
    } catch (e) {
      if (mounted) setState(() => _error = e is ApiException ? e.message : '发送验证码失败');
    }
  }

  Future<void> _login() async {
    if (_isLoading) return;
    setState(() {
      _isLoading = true;
      _error = null;
      _success = null;
    });
    try {
      await _authService.login(LoginRequest(
        principal: _emailController.text,
        credentials: _useCode ? _codeController.text : _passwordController.text,
        loginType: _useCode ? 'email_code' : 'email_password',
      ));
      if (mounted) Navigator.of(context).pushReplacement(MaterialPageRoute(builder: (_) => const ChatPage()));
    } catch (e) {
      if (mounted) setState(() => _error = e is ApiException ? e.message : '登录失败');
    } finally {
      if (mounted) setState(() => _isLoading = false);
    }
  }

  Future<void> _register() async {
    if (_isLoading) return;
    setState(() {
      _isLoading = true;
      _error = null;
      _success = null;
    });
    try {
      await _authService.register(RegisterRequest(
        email: _emailController.text,
        code: _codeController.text,
        password: _passwordController.text,
        nickname: _nicknameController.text,
      ));
      if (mounted) Navigator.of(context).pushReplacement(MaterialPageRoute(builder: (_) => const ChatPage()));
    } catch (e) {
      if (mounted) setState(() => _error = e is ApiException ? e.message : '注册失败');
    } finally {
      if (mounted) setState(() => _isLoading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Container(
        decoration: const BoxDecoration(
          gradient: LinearGradient(
            begin: Alignment.topLeft,
            end: Alignment.bottomRight,
            colors: [Color(0xFF6366F1), Color(0xFF4F46E5)],
          ),
        ),
        child: Center(
          child: SingleChildScrollView(
            padding: const EdgeInsets.all(24),
            child: ConstrainedBox(
              constraints: const BoxConstraints(maxWidth: 420),
              child: Card(
                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
                elevation: 20,
                child: Padding(
                  padding: const EdgeInsets.all(40),
                  child: Column(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      const Text('Vex-Owl', style: TextStyle(fontSize: 28, fontWeight: FontWeight.w700)),
                      const SizedBox(height: 4),
                      Text('AI 智能助手', style: TextStyle(fontSize: 14, color: Colors.grey[500])),
                      const SizedBox(height: 28),
                      _buildTabBar(),
                      const SizedBox(height: 24),
                      if (_error != null) _buildMsg(_error!, const Color(0xFFEF4444)),
                      if (_success != null) _buildMsg(_success!, const Color(0xFF10B981)),
                      const SizedBox(height: 16),
                      if (_isLogin) _buildLoginForm() else _buildRegisterForm(),
                    ],
                  ),
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildTabBar() {
    return Container(
      decoration: BoxDecoration(border: Border(bottom: BorderSide(color: Colors.grey[200]!))),
      child: Row(
        children: [
          _tab('登录', _isLogin, () => _switchMode(true)),
          _tab('注册', !_isLogin, () => _switchMode(false)),
        ],
      ),
    );
  }

  Widget _tab(String label, bool active, VoidCallback onTap) {
    return Expanded(
      child: GestureDetector(
        onTap: onTap,
        child: Container(
          padding: const EdgeInsets.symmetric(vertical: 12),
          decoration: BoxDecoration(
            border: Border(
              bottom: BorderSide(color: active ? const Color(0xFF6366F1) : Colors.transparent, width: 2),
            ),
          ),
          child: Text(label,
              textAlign: TextAlign.center,
              style: TextStyle(
                fontSize: 15,
                fontWeight: FontWeight.w600,
                color: active ? const Color(0xFF6366F1) : Colors.grey[500],
              )),
        ),
      ),
    );
  }

  Widget _buildMsg(String msg, Color color) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(10),
      decoration: BoxDecoration(color: color.withValues(alpha: 0.1), borderRadius: BorderRadius.circular(8)),
      child: Text(msg, style: TextStyle(color: color, fontSize: 14), textAlign: TextAlign.center),
    );
  }

  Widget _buildLoginForm() {
    return Column(
      children: [
        _field('邮箱', _emailController, keyboardType: TextInputType.emailAddress, hint: '请输入邮箱'),
        const SizedBox(height: 16),
        if (!_useCode)
          _field('密码', _passwordController, obscure: true, hint: '请输入密码')
        else ...[
          _field('验证码', _codeController, keyboardType: TextInputType.number, hint: '请输入验证码', maxLength: 6),
          const SizedBox(height: 4),
          Align(
            alignment: Alignment.centerRight,
            child: TextButton(
              onPressed: _codeCountdown > 0 ? null : () => _sendCode('login'),
              child: Text(_codeCountdown > 0 ? '${_codeCountdown}s' : '获取验证码', style: const TextStyle(fontSize: 13)),
            ),
          ),
        ],
        const SizedBox(height: 8),
        SizedBox(
          width: double.infinity,
          height: 48,
          child: ElevatedButton(
            onPressed: _isLoading ? null : _login,
            child: Text(_isLoading ? '登录中...' : '登录'),
          ),
        ),
        const SizedBox(height: 8),
        TextButton(
          onPressed: () => setState(() {
            _useCode = !_useCode;
            _error = null;
            _success = null;
          }),
          child: Text(_useCode ? '密码登录' : '验证码登录', style: const TextStyle(fontSize: 14)),
        ),
      ],
    );
  }

  Widget _buildRegisterForm() {
    return Column(
      children: [
        _field('昵称', _nicknameController, hint: '请输入昵称'),
        const SizedBox(height: 16),
        _field('邮箱', _emailController, keyboardType: TextInputType.emailAddress, hint: '请输入邮箱'),
        const SizedBox(height: 16),
        _field('验证码', _codeController, keyboardType: TextInputType.number, hint: '请输入验证码', maxLength: 6),
        const SizedBox(height: 4),
        Align(
          alignment: Alignment.centerRight,
          child: TextButton(
            onPressed: _codeCountdown > 0 ? null : () => _sendCode('register'),
            child: Text(_codeCountdown > 0 ? '${_codeCountdown}s' : '获取验证码', style: const TextStyle(fontSize: 13)),
          ),
        ),
        const SizedBox(height: 16),
        _field('密码', _passwordController, obscure: true, hint: '请设置密码（至少6位）'),
        const SizedBox(height: 8),
        SizedBox(
          width: double.infinity,
          height: 48,
          child: ElevatedButton(
            onPressed: _isLoading ? null : _register,
            child: Text(_isLoading ? '注册中...' : '注册'),
          ),
        ),
      ],
    );
  }

  Widget _field(String label, TextEditingController controller,
      {TextInputType? keyboardType, bool obscure = false, String? hint, int? maxLength}) {
    return TextField(
      controller: controller,
      keyboardType: keyboardType,
      obscureText: obscure,
      maxLength: maxLength,
      decoration: InputDecoration(labelText: label, hintText: hint, counterText: ''),
    );
  }
}
