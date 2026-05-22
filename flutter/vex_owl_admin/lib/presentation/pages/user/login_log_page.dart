import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/utils/responsive_utils.dart';
import '../../../core/services/user_service.dart';
import '../../../data/models/user_models.dart';
import '../../../data/models/query_models.dart';
import '../../widgets/common/page_container.dart';
import '../../widgets/common/data_table_view.dart';

final loginLogListProvider = FutureProvider.autoDispose<List<LoginRecordEntity>>((ref) async {
  final service = ref.read(userServiceProvider);
  return service.queryLoginLogs(QueriesPageRequest(
    page: QueryPage(page: 0, size: 100),
    order: [OrderBy(field: 'loginTime', direction: 'desc')],
  ));
});

class LoginLogPage extends ConsumerWidget {
  const LoginLogPage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final logsAsync = ref.watch(loginLogListProvider);
    final screenSize = ResponsiveUtils.getScreenSize(context);
    final isMobile = screenSize == ScreenSize.mobile;

    return PageContainer(
      title: '登录日志',
      actions: [
        IconButton(
          icon: const Icon(Icons.refresh),
          onPressed: () => ref.invalidate(loginLogListProvider),
        ),
      ],
      child: logsAsync.when(
        data: (logs) => isMobile
            ? _buildCardList(context, logs)
            : _buildDataTable(context, logs),
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
                onPressed: () => ref.invalidate(loginLogListProvider),
                child: const Text('重试'),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildDataTable(BuildContext context, List<LoginRecordEntity> logs) {
    return DataTableView(
      columns: const [
        DataColumn(label: Text('日志ID')),
        DataColumn(label: Text('主体ID')),
        DataColumn(label: Text('账号ID')),
        DataColumn(label: Text('登录方式')),
        DataColumn(label: Text('登录时间')),
      ],
      rows: logs.map((log) {
        return DataRow(cells: [
          DataCell(Text(log.id, style: const TextStyle(fontSize: 12))),
          DataCell(
            InkWell(
              onTap: () => context.go('/users/detail/${log.subjectId}'),
              child: Text(
                log.subjectId,
                style: const TextStyle(
                  fontSize: 12,
                  color: AppColors.primary,
                  decoration: TextDecoration.underline,
                ),
              ),
            ),
          ),
          DataCell(Text(log.accountId ?? '-', style: const TextStyle(fontSize: 12))),
          DataCell(_buildLoginTypeChip(log.loginType)),
          DataCell(Text(_formatDateTime(log.loginTime))),
        ]);
      }).toList(),
    );
  }

  Widget _buildCardList(BuildContext context, List<LoginRecordEntity> logs) {
    return ListView.builder(
      padding: const EdgeInsets.all(16),
      itemCount: logs.length,
      itemBuilder: (context, index) {
        final log = logs[index];
        return Card(
          margin: const EdgeInsets.only(bottom: 12),
          child: InkWell(
            onTap: () => context.go('/users/detail/${log.subjectId}'),
            borderRadius: BorderRadius.circular(12),
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
                        Row(
                          children: [
                            Expanded(
                              child: Text(
                                log.loginType ?? '未知登录方式',
                                style: const TextStyle(
                                  fontSize: 14,
                                  fontWeight: FontWeight.w600,
                                ),
                              ),
                            ),
                            _buildLoginTypeChip(log.loginType),
                          ],
                        ),
                        const SizedBox(height: 8),
                        Row(
                          children: [
                            Icon(Icons.person_outline, size: 14, color: AppColors.textTertiary),
                            const SizedBox(width: 4),
                            Text(
                              '主体: ${log.subjectId}',
                              style: TextStyle(
                                fontSize: 12,
                                color: AppColors.textSecondary,
                              ),
                            ),
                          ],
                        ),
                        const SizedBox(height: 4),
                        Row(
                          children: [
                            Icon(Icons.access_time, size: 14, color: AppColors.textTertiary),
                            const SizedBox(width: 4),
                            Text(
                              _formatDateTime(log.loginTime),
                              style: TextStyle(
                                fontSize: 12,
                                color: AppColors.textSecondary,
                              ),
                            ),
                          ],
                        ),
                      ],
                    ),
                  ),
                  const Icon(Icons.chevron_right, color: AppColors.textTertiary),
                ],
              ),
            ),
          ),
        );
      },
    );
  }

  Widget _buildLoginTypeChip(String? loginType) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
      decoration: BoxDecoration(
        color: AppColors.info.withValues(alpha: 0.1),
        borderRadius: BorderRadius.circular(4),
      ),
      child: Text(
        loginType ?? '-',
        style: TextStyle(
          fontSize: 12,
          color: AppColors.info,
        ),
      ),
    );
  }

  String _formatDateTime(DateTime? date) {
    if (date == null) return '-';
    return '${date.year}-${date.month.toString().padLeft(2, '0')}-${date.day.toString().padLeft(2, '0')} '
        '${date.hour.toString().padLeft(2, '0')}:${date.minute.toString().padLeft(2, '0')}';
  }
}