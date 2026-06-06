class ChatSession {
  final String id;
  final String tenantId;
  final String title;
  final String status;
  final String lastMessage;
  final String lastMessageTime;
  final int messageCount;
  final bool pinned;
  final bool starred;

  const ChatSession({
    required this.id,
    required this.tenantId,
    required this.title,
    required this.status,
    required this.lastMessage,
    required this.lastMessageTime,
    required this.messageCount,
    required this.pinned,
    required this.starred,
  });

  factory ChatSession.fromJson(Map<String, dynamic> json) => ChatSession(
        id: json['id']?.toString() ?? '',
        tenantId: json['tenantId']?.toString() ?? '',
        title: json['title']?.toString() ?? '',
        status: json['status']?.toString() ?? '',
        lastMessage: json['lastMessage']?.toString() ?? '',
        lastMessageTime: json['lastMessageTime']?.toString() ?? '',
        messageCount: json['messageCount'] is int ? json['messageCount'] : 0,
        pinned: json['pinned'] == true,
        starred: json['starred'] == true,
      );
}

class ChatMessage {
  final String id;
  final String tenantId;
  final String conversationId;
  final String messageType;
  final String textContent;

  const ChatMessage({
    required this.id,
    required this.tenantId,
    required this.conversationId,
    required this.messageType,
    required this.textContent,
  });

  factory ChatMessage.fromJson(Map<String, dynamic> json) => ChatMessage(
        id: json['id']?.toString() ?? '',
        tenantId: json['tenantId']?.toString() ?? '',
        conversationId: json['conversationId']?.toString() ?? '',
        messageType: json['messageType']?.toString() ?? '',
        textContent: json['textContent']?.toString() ?? '',
      );
}
