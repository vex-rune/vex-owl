import 'dart:async';
import '../constants/api_constants.dart';
import '../utils/api_client.dart';
import '../../data/models/chat_models.dart';

class ChatService {
  final _client = ApiClient.instance;

  Stream<String> freeChat(String prompt) {
    return _client.postStream(ApiConstants.freeChat, body: {'prompt': prompt});
  }

  Future<List<ChatSession>> querySessions() async {
    final list = await _client.getList(ApiConstants.sessionList);
    return list.map((e) => ChatSession.fromJson(e as Map<String, dynamic>)).toList();
  }

  Future<List<ChatMessage>> queryMessages(String sessionId) async {
    final list = await _client.getList(ApiConstants.sessionMessages(sessionId));
    return list.map((e) => ChatMessage.fromJson(e as Map<String, dynamic>)).toList();
  }
}
