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

final accountListProvider = FutureProvider.autoDispose<List<AccountEntity>>((ref) async {
  final service = ref.read(userServiceProvider);
  return service.queryAccounts(QueriesPageRequest(
    page: QueryPage(page: 0, size: 100),
  ));
});

class AccountListPage extends ConsumerWidget {
  const AccountListPage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final accountsAsync = ref.watch(accountListProvider);
    final screenSize = ResponsiveUtils.getScreenSize(context);
    final isMobile = screenSize == ScreenSize.mobile;

    return PageContainer(
      title: '账号列表',
      actions: [
        IconButton(
          icon: const Icon(Icons.refresh),
          onPressed: () => ref.invalidate(accountListProvider),
        ),
      ],
      child: accountsAsync.when(
        data: (accounts) => isMobile
            ? _buildCardList(context, accounts)
            : _buildDataTable(context, accounts),
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
                onPressed: () => ref.invalidate(accountListProvider),
                child: const Text('重试'),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildDataTable(BuildContext context, List<AccountEntity> accounts) {
    return DataTableView(
      columns: const [
        DataColumn(label: Text('账号ID')),
        DataColumn(label: Text('主体ID')),
        DataColumn(label: Text('账号')),
        DataColumn(label: Text('类型')),
        DataColumn(label: Text('状态')),
        DataColumn(label: Text('创建时间')),
      ],
      rows: accounts.map((account) {
        return DataRow(cells: [
          DataCell(Text(account.id, style: const TextStyle(fontSize: 12))),
          DataCell(
            InkWell(
              onTap: () => context.go('/users/detail/${account.subjectId}'),
              child: Text(
                account.subjectId,
                style: const TextStyle(
                  fontSize: 12,
                  color: AppColors.primary,
                  decoration: TextDecoration.underline,
                ),
              ),
            ),
          ),
          DataCell(Text(account.account ?? '-')),
          DataCell(Text(account.type ?? '-')),
          DataCell(_buildEnabledChip(account.enabled)),
          DataCell(Text(_formatDate(account.createdAt))),
        ]);
      }).toList(),
    );
  }

  Widget _buildCardList(BuildContext context, List<AccountEntity> accounts) {
    return ListView.builder(
      padding: const EdgeInsets.all(16),
      itemCount: accounts.length,
      itemBuilder: (context, index) {
        final account = accounts[index];
        return Card(
          margin: const EdgeInsets.only(bottom: 12),
          child: InkWell(
            onTap: () => context.go('/users/detail/${account.subjectId}'),
            borderRadius: BorderRadius.circular(12),
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      Expanded(
                        child: Text(
                          account.account ?? '未设置',
                          style: const TextStyle(
                            fontSize: 16,
                            fontWeight: FontWeight.w600,
                          ),
                        ),
                      ),
                      _buildEnabledChip(account.enabled),
                    ],
                  ),
                  const SizedBox(height: 8),
                  Row(
                    children: [
                      Icon(Icons.person_outline, size: 14, color: AppColors.textTertiary),
                      const SizedBox(width: 4),
                      Text(
                        '主体: ${account.subjectId}',
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
                      Icon(Icons.category_outlined, size: 14, color: AppColors.textTertiary),
                      const SizedBox(width: 4),
                      Text(
                        account.type ?? '-',
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
          ),
        );
      },
    );
  }

  Widget _buildEnabledChip(bool? enabled) {
    final isEnabled = enabled == true;
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
      decoration: BoxDecoration(
        color: (isEnabled ? AppColors.success : AppColors.error).withValues(alpha: 0.1),
        borderRadius: BorderRadius.circular(4),
      ),
      child: Text(
        isEnabled ? '启用' : '禁用',
        style: TextStyle(
          fontSize: 12,
          color: isEnabled ? AppColors.success : AppColors.error,
        ),
      ),
    );
  }

  String _formatDate(DateTime? date) {
    if (date == null) return '-';
    return '${date.year}-${date.month.toString().padLeft(2, '0')}-${date.day.toString().padLeft(2, '0')}';
  }
}