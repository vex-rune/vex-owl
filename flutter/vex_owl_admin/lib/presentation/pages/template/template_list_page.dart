import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/utils/responsive_utils.dart';
import '../../../core/services/template_service.dart';
import '../../../data/models/template_models.dart';
import '../../../data/models/query_models.dart';
import '../../widgets/common/page_container.dart';
import '../../widgets/common/data_table_view.dart';

final templateListProvider = FutureProvider.autoDispose<List<TemplateEntity>>((ref) async {
  final service = ref.read(templateServiceProvider);
  return service.queryTemplates(QueriesPageRequest(
    page: QueryPage(page: 0, size: 100),
  ));
});

class TemplateListPage extends ConsumerWidget {
  const TemplateListPage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final templatesAsync = ref.watch(templateListProvider);
    final screenSize = ResponsiveUtils.getScreenSize(context);
    final isMobile = screenSize == ScreenSize.mobile;

    return PageContainer(
      title: '通知模板',
      actions: [
        IconButton(
          icon: const Icon(Icons.refresh),
          onPressed: () => ref.invalidate(templateListProvider),
        ),
      ],
      child: templatesAsync.when(
        data: (templates) => isMobile
            ? _buildCardList(context, ref, templates)
            : _buildDataTable(context, ref, templates),
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
                onPressed: () => ref.invalidate(templateListProvider),
                child: const Text('重试'),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildDataTable(BuildContext context, WidgetRef ref, List<TemplateEntity> templates) {
    return DataTableView(
      columns: const [
        DataColumn(label: Text('模板名称')),
        DataColumn(label: Text('编码')),
        DataColumn(label: Text('备注')),
        DataColumn(label: Text('状态')),
        DataColumn(label: Text('创建时间')),
        DataColumn(label: Text('操作')),
      ],
      rows: templates.map((template) {
        return DataRow(cells: [
          DataCell(Text(template.name)),
          DataCell(Text(template.code, style: const TextStyle(fontSize: 12))),
          DataCell(Text(template.remark ?? '-', style: const TextStyle(fontSize: 12))),
          DataCell(_buildStatusChip(template.enabled)),
          DataCell(Text(_formatDate(template.createdAt))),
          DataCell(
            Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                TextButton(
                  onPressed: () => context.go('/templates/preview/${template.id}'),
                  child: const Text('预览'),
                ),
                TextButton(
                  onPressed: () => context.go('/templates/edit/${template.id}'),
                  child: const Text('编辑'),
                ),
                TextButton(
                  onPressed: () => _handleDelete(context, ref, template),
                  child: const Text('删除', style: TextStyle(color: AppColors.error)),
                ),
              ],
            ),
          ),
        ]);
      }).toList(),
    );
  }

  Widget _buildCardList(BuildContext context, WidgetRef ref, List<TemplateEntity> templates) {
    return ListView.builder(
      padding: const EdgeInsets.all(16),
      itemCount: templates.length,
      itemBuilder: (context, index) {
        final template = templates[index];
        return Card(
          margin: const EdgeInsets.only(bottom: 12),
          child: Padding(
            padding: const EdgeInsets.all(16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            template.name,
                            style: const TextStyle(
                              fontSize: 16,
                              fontWeight: FontWeight.w600,
                            ),
                          ),
                          const SizedBox(height: 4),
                          Text(
                            template.code,
                            style: TextStyle(
                              fontSize: 12,
                              color: AppColors.textSecondary,
                            ),
                          ),
                        ],
                      ),
                    ),
                    _buildStatusChip(template.enabled),
                  ],
                ),
                if (template.remark != null && template.remark!.isNotEmpty) ...[
                  const SizedBox(height: 12),
                  Text(
                    template.remark!,
                    style: TextStyle(
                      fontSize: 14,
                      color: AppColors.textSecondary,
                    ),
                  ),
                ],
                const SizedBox(height: 16),
                Row(
                  mainAxisAlignment: MainAxisAlignment.end,
                  children: [
                    TextButton.icon(
                      onPressed: () => context.go('/templates/preview/${template.id}'),
                      icon: const Icon(Icons.visibility_outlined, size: 18),
                      label: const Text('预览'),
                    ),
                    const SizedBox(width: 8),
                    TextButton.icon(
                      onPressed: () => context.go('/templates/edit/${template.id}'),
                      icon: const Icon(Icons.edit_outlined, size: 18),
                      label: const Text('编辑'),
                    ),
                    const SizedBox(width: 8),
                    TextButton.icon(
                      onPressed: () => _handleDelete(context, ref, template),
                      icon: Icon(Icons.delete_outlined, size: 18, color: AppColors.error),
                      label: Text('删除', style: TextStyle(color: AppColors.error)),
                    ),
                  ],
                ),
              ],
            ),
          ),
        );
      },
    );
  }

  Widget _buildStatusChip(bool? enabled) {
    final isEnabled = enabled == true;
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
      decoration: BoxDecoration(
        color: (isEnabled ? AppColors.success : AppColors.textTertiary).withValues(alpha: 0.1),
        borderRadius: BorderRadius.circular(4),
      ),
      child: Text(
        isEnabled ? '启用' : '禁用',
        style: TextStyle(
          fontSize: 12,
          color: isEnabled ? AppColors.success : AppColors.textTertiary,
        ),
      ),
    );
  }

  Future<void> _handleDelete(BuildContext context, WidgetRef ref, TemplateEntity template) async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('确认删除'),
        content: Text('确定要删除模板 "${template.name}" 吗？'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context, false),
            child: const Text('取消'),
          ),
          ElevatedButton(
            onPressed: () => Navigator.pop(context, true),
            style: ElevatedButton.styleFrom(backgroundColor: AppColors.error),
            child: const Text('删除'),
          ),
        ],
      ),
    );

    if (confirmed == true) {
      try {
        await ref.read(templateServiceProvider).deleteTemplate(template.id);
        if (context.mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(content: Text('删除成功')),
          );
          ref.invalidate(templateListProvider);
        }
      } catch (e) {
        if (context.mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(content: Text('删除失败: $e')),
          );
        }
      }
    }
  }

  String _formatDate(DateTime? date) {
    if (date == null) return '-';
    return '${date.year}-${date.month.toString().padLeft(2, '0')}-${date.day.toString().padLeft(2, '0')}';
  }
}