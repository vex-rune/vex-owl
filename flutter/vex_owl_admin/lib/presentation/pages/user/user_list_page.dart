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

final subjectListProvider = FutureProvider.autoDispose<List<SubjectEntity>>((ref) async {
  final service = ref.read(userServiceProvider);
  return service.querySubjects(QueriesPageRequest(
    page: QueryPage(page: 0, size: 100),
  ));
});

class UserListPage extends ConsumerWidget {
  const UserListPage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final subjectsAsync = ref.watch(subjectListProvider);
    final screenSize = ResponsiveUtils.getScreenSize(context);
    final isMobile = screenSize == ScreenSize.mobile;

    return PageContainer(
      title: '主体列表',
      actions: [
        IconButton(
          icon: const Icon(Icons.refresh),
          onPressed: () => ref.invalidate(subjectListProvider),
        ),
      ],
      child: subjectsAsync.when(
        data: (subjects) => isMobile
            ? _buildCardList(context, subjects)
            : _buildDataTable(context, subjects),
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
                onPressed: () => ref.invalidate(subjectListProvider),
                child: const Text('重试'),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildDataTable(BuildContext context, List<SubjectEntity> subjects) {
    return DataTableView(
      columns: const [
        DataColumn(label: Text('ID')),
        DataColumn(label: Text('名称')),
        DataColumn(label: Text('类型')),
        DataColumn(label: Text('状态')),
        DataColumn(label: Text('创建时间')),
        DataColumn(label: Text('操作')),
      ],
      rows: subjects.map((subject) {
        return DataRow(cells: [
          DataCell(Text(subject.id, style: const TextStyle(fontSize: 12))),
          DataCell(Text(subject.name ?? '-')),
          DataCell(Text(subject.type ?? '-')),
          DataCell(_buildStatusChip(subject.status)),
          DataCell(Text(_formatDate(subject.createdAt))),
          DataCell(
            TextButton(
              onPressed: () => context.go('/users/detail/${subject.id}'),
              child: const Text('查看详情'),
            ),
          ),
        ]);
      }).toList(),
    );
  }

  Widget _buildCardList(BuildContext context, List<SubjectEntity> subjects) {
    return ListView.builder(
      padding: const EdgeInsets.all(16),
      itemCount: subjects.length,
      itemBuilder: (context, index) {
        final subject = subjects[index];
        return Card(
          margin: const EdgeInsets.only(bottom: 12),
          child: InkWell(
            onTap: () => context.go('/users/detail/${subject.id}'),
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
                          subject.name ?? '未命名',
                          style: const TextStyle(
                            fontSize: 16,
                            fontWeight: FontWeight.w600,
                          ),
                        ),
                      ),
                      _buildStatusChip(subject.status),
                    ],
                  ),
                  const SizedBox(height: 8),
                  Text(
                    'ID: ${subject.id}',
                    style: TextStyle(
                      fontSize: 12,
                      color: AppColors.textSecondary,
                    ),
                  ),
                  const SizedBox(height: 4),
                  Row(
                    children: [
                      Icon(Icons.category_outlined, size: 14, color: AppColors.textTertiary),
                      const SizedBox(width: 4),
                      Text(
                        subject.type ?? '-',
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

  Widget _buildStatusChip(String? status) {
    final isActive = status?.toLowerCase() == 'active';
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
      decoration: BoxDecoration(
        color: (isActive ? AppColors.success : AppColors.textTertiary).withValues(alpha: 0.1),
        borderRadius: BorderRadius.circular(4),
      ),
      child: Text(
        status ?? '-',
        style: TextStyle(
          fontSize: 12,
          color: isActive ? AppColors.success : AppColors.textTertiary,
        ),
      ),
    );
  }

  String _formatDate(DateTime? date) {
    if (date == null) return '-';
    return '${date.year}-${date.month.toString().padLeft(2, '0')}-${date.day.toString().padLeft(2, '0')}';
  }
}