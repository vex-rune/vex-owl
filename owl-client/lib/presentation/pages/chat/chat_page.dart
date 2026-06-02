import 'package:flutter/material.dart';
import '../../../core/services/auth_service.dart';
import '../../../core/services/chat_service.dart';
import '../../../core/theme/app_theme.dart';
import '../../../core/utils/storage.dart';
import '../../../data/models/auth_models.dart';
import '../../../data/models/chat_models.dart';
import '../auth/auth_page.dart';

class ChatPage extends StatefulWidget {
  const ChatPage({super.key});

  @override
  State<ChatPage> createState() => _ChatPageState();
}

class _ChatPageState extends State<ChatPage> {
  final _chatService = ChatService();
  final _authService = AuthService();
  final _inputController = TextEditingController();
  final _scrollController = ScrollController();
  final _focusNode = FocusNode();

  List<ChatSession> _sessions = [];
  final List<_ChatMsg> _messages = [];
  String? _activeSessionId;
  bool _isStreaming = false;
  bool _isLoadingSessions = false;
  bool _isLoadingMessages = false;
  String? _error;

  bool get _isWideScreen => MediaQuery.of(context).size.width > 768;

  @override
  void initState() {
    super.initState();
    _loadSessions();
  }

  @override
  void dispose() {
    _inputController.dispose();
    _scrollController.dispose();
    _focusNode.dispose();
    super.dispose();
  }

  UserInfo? get _user => Storage.getUserInfo() != null ? UserInfo.fromJson(Storage.getUserInfo()!) : null;

  Future<void> _loadSessions() async {
    setState(() => _isLoadingSessions = true);
    try {
      _sessions = await _chatService.querySessions();
    } catch (_) {}
    if (mounted) setState(() => _isLoadingSessions = false);
  }

  Future<void> _selectSession(String sessionId) async {
    setState(() {
      _activeSessionId = sessionId;
      _isLoadingMessages = true;
      _messages.clear();
      _error = null;
    });
    try {
      final raw = await _chatService.queryMessages(sessionId);
      setState(() {
        _messages.addAll(raw.map((m) => _ChatMsg(
              role: m.messageType == 'USER' ? _Role.user : _Role.assistant,
              text: m.textContent,
            )));
        _isLoadingMessages = false;
      });
      _scrollToBottom();
    } catch (e) {
      setState(() {
        _isLoadingMessages = false;
        _error = '加载消息失败';
      });
    }
  }

  void _newChat() {
    setState(() {
      _activeSessionId = null;
      _messages.clear();
      _error = null;
    });
    _focusNode.requestFocus();
  }

  Future<void> _sendMessage() async {
    final text = _inputController.text.trim();
    if (text.isEmpty || _isStreaming) return;

    final sessionId = _activeSessionId ?? 'free-default';
    if (_activeSessionId == null) {
      setState(() => _activeSessionId = sessionId);
    }

    _inputController.clear();
    setState(() {
      _messages.add(_ChatMsg(role: _Role.user, text: text));
      _messages.add(_ChatMsg(role: _Role.assistant, text: '', streaming: true));
      _isStreaming = true;
      _error = null;
    });
    _scrollToBottom();

    final aiIndex = _messages.length - 1;

    try {
      await for (final chunk in _chatService.freeChat(text)) {
        if (!mounted) return;
        setState(() {
          _messages[aiIndex] = _ChatMsg(
            role: _Role.assistant,
            text: _messages[aiIndex].text + chunk,
            streaming: true,
          );
        });
        _scrollToBottom();
      }
      setState(() {
        _messages[aiIndex] = _ChatMsg(role: _Role.assistant, text: _messages[aiIndex].text);
        _isStreaming = false;
      });
    } catch (e) {
      setState(() {
        if (_messages[aiIndex].text.isEmpty) {
          _messages.removeAt(aiIndex);
        } else {
          _messages[aiIndex] = _ChatMsg(role: _Role.assistant, text: _messages[aiIndex].text);
        }
        _isStreaming = false;
        _error = e.toString();
      });
    }
  }

  void _scrollToBottom() {
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (_scrollController.hasClients) {
        _scrollController.animateTo(
          _scrollController.position.maxScrollExtent,
          duration: const Duration(milliseconds: 200),
          curve: Curves.easeOut,
        );
      }
    });
  }

  Future<void> _logout() async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('退出登录'),
        content: const Text('确定要退出登录吗？'),
        actions: [
          TextButton(onPressed: () => Navigator.pop(ctx, false), child: const Text('取消')),
          TextButton(onPressed: () => Navigator.pop(ctx, true), child: const Text('确定')),
        ],
      ),
    );
    if (confirmed == true) {
      await _authService.logout();
      if (mounted) Navigator.of(context).pushReplacement(MaterialPageRoute(builder: (_) => const AuthPage()));
    }
  }

  @override
  Widget build(BuildContext context) {
    if (_isWideScreen) {
      return _buildWideLayout();
    }
    return _buildMobileLayout();
  }

  Widget _buildWideLayout() {
    return Scaffold(
      body: Row(
        children: [
          _buildSidebar(width: 260),
          Expanded(child: _buildChatArea()),
        ],
      ),
    );
  }

  Widget _buildMobileLayout() {
    return Scaffold(
      appBar: AppBar(
        leading: Builder(
          builder: (ctx) => IconButton(
            icon: const Icon(Icons.menu),
            onPressed: () => Scaffold.of(ctx).openDrawer(),
          ),
        ),
        title: const Text('Vex-Owl'),
        actions: [
          IconButton(
            icon: const Icon(Icons.add),
            tooltip: '新对话',
            onPressed: _newChat,
          ),
        ],
      ),
      drawer: _buildSidebar(width: 280),
      body: _buildChatArea(),
    );
  }

  Widget _buildSidebar({required double width}) {
    final userInitial = _user?.nickname.isNotEmpty == true ? _user!.nickname[0] : '?';
    return Container(
      width: width,
      color: AppColors.sidebarBg,
      child: Column(
        children: [
          Padding(
            padding: const EdgeInsets.all(12),
            child: SizedBox(
              width: double.infinity,
              child: ElevatedButton.icon(
                onPressed: _newChat,
                icon: const Icon(Icons.add, size: 18),
                label: const Text('新对话'),
              ),
            ),
          ),
          Expanded(
            child: _isLoadingSessions
                ? const Center(child: CircularProgressIndicator())
                : _sessions.isEmpty
                    ? const Center(
                        child: Text('暂无会话记录', style: TextStyle(color: AppColors.textTertiary, fontSize: 13)))
                    : ListView.builder(
                        padding: const EdgeInsets.symmetric(horizontal: 8),
                        itemCount: _sessions.length,
                        itemBuilder: (ctx, i) {
                          final s = _sessions[i];
                          final active = s.id == _activeSessionId;
                          return ListTile(
                            dense: true,
                            selected: active,
                            selectedTileColor: AppColors.sidebarActive,
                            title: Text(s.title.isNotEmpty ? s.title : '新对话',
                                style: const TextStyle(fontSize: 14),
                                maxLines: 1,
                                overflow: TextOverflow.ellipsis),
                            subtitle: Text('${s.messageCount} 条消息',
                                style: const TextStyle(fontSize: 12, color: AppColors.textTertiary)),
                            onTap: () {
                              _selectSession(s.id);
                              if (!_isWideScreen) Navigator.pop(context);
                            },
                          );
                        },
                      ),
          ),
          const Divider(height: 1),
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
            child: Row(
              children: [
                CircleAvatar(
                  radius: 16,
                  backgroundColor: AppColors.primary,
                  child: Text(userInitial, style: const TextStyle(color: Colors.white, fontSize: 14)),
                ),
                const SizedBox(width: 10),
                Expanded(
                  child: Text(
                    _user?.nickname ?? _user?.email ?? '用户',
                    style: const TextStyle(fontSize: 14),
                    overflow: TextOverflow.ellipsis,
                  ),
                ),
                TextButton(
                  onPressed: _logout,
                  child: const Text('退出', style: TextStyle(fontSize: 12)),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildChatArea() {
    return Column(
      children: [
        Expanded(
          child: _messages.isEmpty && !_isLoadingMessages
              ? _buildWelcome()
              : _buildMessageList(),
        ),
        if (_error != null)
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 4),
            child: Text(_error!, style: const TextStyle(color: AppColors.error, fontSize: 13)),
          ),
        _buildInputArea(),
      ],
    );
  }

  Widget _buildWelcome() {
    return const Center(
      child: Padding(
        padding: EdgeInsets.all(40),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Text('你好，我是奥沃', style: TextStyle(fontSize: 24, fontWeight: FontWeight.w600)),
            SizedBox(height: 12),
            Text(
              '一个由 VEX 技术团队打造的 AI 助手。\n我可以帮你回答问题、编写代码、分析数据、撰写文档等。',
              textAlign: TextAlign.center,
              style: TextStyle(fontSize: 15, color: AppColors.textSecondary, height: 1.6),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildMessageList() {
    return ListView.builder(
      controller: _scrollController,
      padding: const EdgeInsets.symmetric(vertical: 20),
      itemCount: _messages.length + (_isLoadingMessages ? 1 : 0),
      itemBuilder: (ctx, i) {
        if (_isLoadingMessages && i == _messages.length) {
          return const Padding(
            padding: EdgeInsets.all(20),
            child: Center(child: CircularProgressIndicator()),
          );
        }
        return _buildMessage(_messages[i]);
      },
    );
  }

  Widget _buildMessage(_ChatMsg msg) {
    final isUser = msg.role == _Role.user;
    final userInitial = _user?.nickname.isNotEmpty == true ? _user!.nickname[0] : '?';

    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 8),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        mainAxisAlignment: isUser ? MainAxisAlignment.end : MainAxisAlignment.start,
        children: [
          if (!isUser)
            const CircleAvatar(
              radius: 16,
              backgroundColor: AppColors.bgTertiary,
              child: Text('OW', style: TextStyle(fontSize: 12, fontWeight: FontWeight.w600)),
            ),
          if (!isUser) const SizedBox(width: 12),
          Flexible(
            child: Container(
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
              decoration: BoxDecoration(
                color: isUser ? AppColors.userBubble : AppColors.assistantBubble,
                borderRadius: BorderRadius.circular(12),
              ),
              child: Row(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Flexible(
                    child: SelectableText(
                      msg.text,
                      style: TextStyle(
                        fontSize: 15,
                        height: 1.6,
                        color: isUser ? Colors.white : AppColors.textPrimary,
                      ),
                    ),
                  ),
                  if (msg.streaming) ...[
                    const SizedBox(width: 4),
                    _BlinkingCursor(color: isUser ? Colors.white : AppColors.primary),
                  ],
                ],
              ),
            ),
          ),
          if (isUser) const SizedBox(width: 12),
          if (isUser)
            CircleAvatar(
              radius: 16,
              backgroundColor: AppColors.primary,
              child: Text(userInitial, style: const TextStyle(color: Colors.white, fontSize: 14)),
            ),
        ],
      ),
    );
  }

  Widget _buildInputArea() {
    return Container(
      decoration: const BoxDecoration(
        border: Border(top: BorderSide(color: AppColors.border)),
        color: AppColors.bgPrimary,
      ),
      padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 12),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.end,
        children: [
          Expanded(
            child: TextField(
              controller: _inputController,
              focusNode: _focusNode,
              maxLines: 4,
              minLines: 1,
              enabled: !_isStreaming,
              decoration: const InputDecoration(
                hintText: '输入你的问题... (Enter 发送)',
                border: OutlineInputBorder(borderRadius: BorderRadius.all(Radius.circular(12))),
                contentPadding: EdgeInsets.symmetric(horizontal: 16, vertical: 12),
              ),
              onSubmitted: (_) => _sendMessage(),
              textInputAction: TextInputAction.send,
            ),
          ),
          const SizedBox(width: 12),
          SizedBox(
            width: 48,
            height: 48,
            child: IconButton.filled(
              onPressed: (_inputController.text.trim().isEmpty || _isStreaming) ? null : _sendMessage,
              icon: const Icon(Icons.send, size: 20),
              style: IconButton.styleFrom(
                backgroundColor: _inputController.text.trim().isEmpty || _isStreaming
                    ? AppColors.bgTertiary
                    : AppColors.primary,
                foregroundColor: _inputController.text.trim().isEmpty || _isStreaming
                    ? AppColors.textTertiary
                    : Colors.white,
              ),
            ),
          ),
        ],
      ),
    );
  }
}

enum _Role { user, assistant }

class _ChatMsg {
  final _Role role;
  final String text;
  final bool streaming;
  _ChatMsg({required this.role, required this.text, this.streaming = false});
}

class _BlinkingCursor extends StatefulWidget {
  final Color color;
  const _BlinkingCursor({required this.color});

  @override
  State<_BlinkingCursor> createState() => _BlinkingCursorState();
}

class _BlinkingCursorState extends State<_BlinkingCursor>
    with SingleTickerProviderStateMixin {
  late AnimationController _controller;
  late Animation<double> _animation;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(duration: const Duration(milliseconds: 800), vsync: this)..repeat(reverse: true);
    _animation = Tween<double>(begin: 1, end: 0).animate(_controller);
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder(
      animation: _animation,
      builder: (_, __) => Container(
        width: 2,
        height: 16,
        color: widget.color.withValues(alpha: _animation.value),
      ),
    );
  }
}
