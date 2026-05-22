import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/utils/responsive_utils.dart';
import '../../../core/services/user_service.dart';
import '../../../data/models/user_models.dart';
import '../../../data/models/query_models.dart';
import '../../widgets/common/page_container.dart';

final userDetailProvider = FutureProvider.family<Map<String, dynamic>, String>((ref, subjectId) async {
  final service = ref.read(userServiceProvider);
  
  final userFuture = service.getUser(subjectId).catchError((_) async => null as UserProfileEntity?);
  final subjectsFuture = service.querySubjects(QueriesPageRequest(
    predicate: [Predicate(field: 'id', op: 'eq', value: subjectId)],
    page: QueryPage(page: 0, size: 1),
  )).catchError((_) async => <SubjectEntity>[]);
  final accountsFuture = service.queryAccounts(QueriesPageRequest(
    predicate: [Predicate(field: 'subjectId', op: 'eq', value: subjectId)],
    page: QueryPage(page: 0, size: 100),
  )).catchError((_) async => <AccountEntity>[]);
  final logsFuture = service.queryLoginLogs(QueriesPageRequest(
    predicate: [Predicate(field: 'subjectId', op: 'eq', value: subjectId)],
    page: QueryPage(page: 0, size: 100),
    order: [OrderBy(field: 'loginTime', direction: 'desc')],
  )).catchError((_) async => <LoginRecordEntity>[]);

  final results = await Future.wait([
    userFuture,
    subjectsFuture,
    accountsFuture,
    logsFuture,
  ]);

  return {
    'user': results[0] as UserProfileEntity?,
    'subjects': results[1] as List<SubjectEntity>,
    'accounts': results[2] as List<AccountEntity>,
    'logs': results[3] as List<LoginRecordEntity>,
  };
});

class UserDetailPage extends ConsumerStatefulWidget {
  final String subjectId;

  const UserDetailPage({super.key, required this.subjectId});

  @override
  ConsumerState<UserDetailPage> createState() => _UserDetailPageState();
}

class _UserDetailPageState extends ConsumerState<UserDetailPage> with SingleTickerProviderStateMixin {
  late TabController _tabController;

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 4, vsync: this);
  }

  @override
  void dispose() {
    _tabController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final detailAsync = ref.watch(userDetailProvider(widget.subjectId));
    final screenSize = ResponsiveUtils.getScreenSize(context);
    final isMobile = screenSize == ScreenSize.mobile;

    return PageContainer(
      title: '用户详情',
      child: detailAsync.when(
        data: (data) => _buildContent(context, data, isMobile),
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (error, stack) => Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              const Icon(Icons.error_outline, size: 48, color: AppColors.error),
              const SizedBox(height: 16),
              Text('加载失败: $error'),
              const SizedBox(height: 16),
              ElevatedButton(
                onPressed: () => ref.invalidate(userDetailProvider(widget.subjectId)),
                child: const Text('重试'),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildContent(BuildContext context, Map<String, dynamic> data, bool isMobile) {
    final user = data['user'] as UserProfileEntity?;
    final subjects = data['subjects'] as List<SubjectEntity>;
    final accounts = data['accounts'] as List<AccountEntity>;
    final logs = data['logs'] as List<LoginRecordEntity>;

    if (isMobile) {
      return _buildMobileContent(user, subjects, accounts, logs);
    }

    return Column(
      children: [
        Container(
          color: AppColors.bgPrimary,
          child: TabBar(
            controller: _tabController,
            labelColor: AppColors.primary,
            unselectedLabelColor: AppColors.textSecondary,
            indicatorColor: AppColors.primary,
            tabs: const [
              Tab(text: '主体信息'),
              Tab(text: '用户信息'),
              Tab(text: '账号'),
              Tab(text: '登录日志'),
            ],
          ),
        ),
        Expanded(
          child: TabBarView(
            controller: _tabController,
            children: [
              _buildSubjectTab(subjects),
              _buildUserTab(user),
              _buildAccountTab(accounts),
              _buildLoginLogTab(logs),
            ],
          ),
        ),
      ],
    );
  }

  Widget _buildMobileContent(
    UserProfileEntity? user,
    List<SubjectEntity> subjects,
    List<AccountEntity> accounts,
    List<LoginRecordEntity> logs,
  ) {
    return DefaultTabController(
      length: 4,
      child: Column(
        children: [
          Container(
            color: AppColors.bgPrimary,
            child: TabBar(
              labelColor: AppColors.primary,
              unselectedLabelColor: AppColors.textSecondary,
              indicatorColor: AppColors.primary,
              isScrollable: true,
              tabs: const [
                Tab(text: '主体'),
                Tab(text: '用户'),
                Tab(text: '账号'),
                Tab(text: '日志'),
              ],
            ),
          ),
          Expanded(
            child: TabBarView(
              children: [
                _buildSubjectTab(subjects),
                _buildUserTab(user),
                _buildAccountTab(accounts),
                _buildLoginLogTab(logs),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildSubjectTab(List<SubjectEntity> subjects) {
    if (subjects.isEmpty) {
      return const Center(child: Text('暂无主体信息'));
    }
    final subject = subjects.first;
    return SingleChildScrollView(
      padding: const EdgeInsets.all(16),
      child: Card(
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              _buildInfoRow('主体ID', subject.id),
              _buildInfoRow('名称', subject.name ?? '-'),
              _buildInfoRow('类型', subject.type ?? '-'),
              _buildInfoRow('状态', subject.status ?? '-'),
              _buildInfoRow('创建时间', _formatDateTime(subject.createdAt)),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildUserTab(UserProfileEntity? user) {
    if (user == null) {
      return const Center(child: Text('暂无用户信息'));
    }
    return SingleChildScrollView(
      padding: const EdgeInsets.all(16),
      child: Card(
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              _buildInfoRow('用户ID', user.id),
              _buildInfoRow('昵称', user.nickname ?? '-'),
              _buildInfoRow('邮箱', user.email ?? '-'),
              _buildInfoRow('手机', user.phone ?? '-'),
              _buildInfoRow('状态', user.status ?? '-'),
              _buildInfoRow('创建时间', _formatDateTime(user.createdAt)),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildAccountTab(List<AccountEntity> accounts) {
    if (accounts.isEmpty) {
      return const Center(child: Text('暂无账号信息'));
    }
    return ListView.builder(
      padding: const EdgeInsets.all(16),
      itemCount: accounts.length,
      itemBuilder: (context, index) {
        final account = accounts[index];
        return Card(
          margin: const EdgeInsets.only(bottom: 12),
          child: Padding(
            padding: const EdgeInsets.all(16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                _buildInfoRow('账号ID', account.id),
                _buildInfoRow('账号', account.account ?? '-'),
                _buildInfoRow('类型', account.type ?? '-'),
                _buildInfoRow('启用状态', account.enabled == true ? '启用' : '禁用'),
                _buildInfoRow('创建时间', _formatDateTime(account.createdAt)),
              ],
            ),
          ),
        );
      },
    );
  }

  Widget _buildLoginLogTab(List<LoginRecordEntity> logs) {
    if (logs.isEmpty) {
      return const Center(child: Text('暂无登录日志'));
    }
    return ListView.builder(
      padding: const EdgeInsets.all(16),
      itemCount: logs.length,
      itemBuilder: (context, index) {
        final log = logs[index];
        return Card(
          margin: const EdgeInsets.only(bottom: 12),
          child: Padding(
            padding: const EdgeInsets.all(16),
            child: Row(
              children: [
                Container(
                  width: 48,
                  height: 48,
                  decoration: BoxDecoration(
                    color: AppColors.primary.withValues(alpha: 0.1),
                    borderRadius: BorderRadius.circular(8),
                  ),
                  child: const Icon(Icons.login, color: AppColors.primary),
                ),
                const SizedBox(width: 16),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        log.loginType ?? '未知登录',
                        style: const TextStyle(
                          fontSize: 14,
                          fontWeight: FontWeight.w600,
                        ),
                      ),
                      const SizedBox(height: 4),
                      Text(
                        _formatDateTime(log.loginTime),
                        style: TextStyle(
                          fontSize: 12,
                          color: AppColors.textSecondary,
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),
          ),
        );
      },
    );
  }

  Widget _buildInfoRow(String label, String value) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 12),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          SizedBox(
            width: 100,
            child: Text(
              label,
              style: TextStyle(
                color: AppColors.textSecondary,
                fontSize: 14,
              ),
            ),
          ),
          Expanded(
            child: Text(
              value,
              style: const TextStyle(
                fontSize: 14,
                fontWeight: FontWeight.w500,
              ),
            ),
          ),
        ],
      ),
    );
  }

  String _formatDateTime(DateTime? date) {
    if (date == null) return '-';
    return '${date.year}-${date.month.toString().padLeft(2, '0')}-${date.day.toString().padLeft(2, '0')} '
        '${date.hour.toString().padLeft(2, '0')}:${date.minute.toString().padLeft(2, '0')}';
  }
}