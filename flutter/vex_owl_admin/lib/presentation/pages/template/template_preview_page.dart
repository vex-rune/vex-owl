import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/services/template_service.dart';
import '../../../data/models/template_models.dart';
import '../../widgets/common/page_container.dart';

final templatePreviewProvider = FutureProvider.family<TemplateEntity?, String>((ref, id) async {
  try {
    return await ref.read(templateServiceProvider).getTemplate(id);
  } catch (e) {
    return null;
  }
});

class TemplatePreviewPage extends ConsumerStatefulWidget {
  final String templateId;

  const TemplatePreviewPage({super.key, required this.templateId});

  @override
  ConsumerState<TemplatePreviewPage> createState() => _TemplatePreviewPageState();
}

class _TemplatePreviewPageState extends ConsumerState<TemplatePreviewPage> {
  final Map<String, TextEditingController> _paramControllers = {};
  String _previewContent = '';

  @override
  void dispose() {
    for (final controller in _paramControllers.values) {
      controller.dispose();
    }
    super.dispose();
  }

  List<String> _extractParams(String content) {
    final regex = RegExp(r'\{\{(\w+)\}\}');
    final matches = regex.allMatches(content);
    return matches.map((m) => m.group(1)!).toSet().toList();
  }

  void _updatePreview(String content) {
    String result = content;
    for (final entry in _paramControllers.entries) {
      result = result.replaceAll('{{${entry.key}}}', entry.value.text.isEmpty ? '[${entry.key}]' : entry.value.text);
    }
    setState(() {
      _previewContent = result;
    });
  }

  @override
  Widget build(BuildContext context) {
    final templateAsync = ref.watch(templatePreviewProvider(widget.templateId));

    return PageContainer(
      title: '模板预览',
      actions: [
        TextButton.icon(
          onPressed: () => context.go('/templates/edit/${widget.templateId}'),
          icon: const Icon(Icons.edit),
          label: const Text('编辑'),
        ),
        TextButton.icon(
          onPressed: () => context.go('/templates'),
          icon: const Icon(Icons.arrow_back),
          label: const Text('返回'),
        ),
      ],
      child: templateAsync.when(
        data: (template) => template == null
            ? const Center(child: Text('模板不存在'))
            : _buildPreview(template),
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (error, stack) => Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              const Icon(Icons.error_outline, size: 48, color: AppColors.error),
              const SizedBox(height: 16),
              Text('加载失败: $error'),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildPreview(TemplateEntity template) {
    final params = _extractParams(template.content ?? '');

    for (final param in params) {
      if (!_paramControllers.containsKey(param)) {
        _paramControllers[param] = TextEditingController();
        _paramControllers[param]!.addListener(() => _updatePreview(template.content ?? ''));
      }
    }

    _previewContent = template.content ?? '';

    return SingleChildScrollView(
      padding: const EdgeInsets.all(24),
      child: Center(
        child: Container(
          constraints: const BoxConstraints(maxWidth: 800),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              Card(
                child: Padding(
                  padding: const EdgeInsets.all(24),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        children: [
                          const Icon(Icons.info_outline, color: AppColors.info),
                          const SizedBox(width: 8),
                          const Text(
                            '模板信息',
                            style: TextStyle(
                              fontSize: 18,
                              fontWeight: FontWeight.w600,
                            ),
                          ),
                          const Spacer(),
                          Container(
                            padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                            decoration: BoxDecoration(
                              color: (template.enabled == true ? AppColors.success : AppColors.textTertiary)
                                  .withValues(alpha: 0.1),
                              borderRadius: BorderRadius.circular(16),
                            ),
                            child: Text(
                              template.enabled == true ? '启用' : '禁用',
                              style: TextStyle(
                                fontSize: 12,
                                color: template.enabled == true ? AppColors.success : AppColors.textTertiary,
                                fontWeight: FontWeight.w500,
                              ),
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(height: 16),
                      _buildInfoRow('模板名称', template.name),
                      _buildInfoRow('模板编码', template.code),
                      if (template.remark != null && template.remark!.isNotEmpty)
                        _buildInfoRow('备注', template.remark!),
                    ],
                  ),
                ),
              ),
              if (params.isNotEmpty) ...[
                const SizedBox(height: 24),
                Card(
                  child: Padding(
                    padding: const EdgeInsets.all(24),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        const Row(
                          children: [
                            Icon(Icons.edit_note, color: AppColors.primary),
                            SizedBox(width: 8),
                            Text(
                              '参数设置',
                              style: TextStyle(
                                fontSize: 18,
                                fontWeight: FontWeight.w600,
                              ),
                            ),
                          ],
                        ),
                        const SizedBox(height: 16),
                        Wrap(
                          spacing: 16,
                          runSpacing: 16,
                          children: params.map((param) {
                            return SizedBox(
                              width: 200,
                              child: TextField(
                                controller: _paramControllers[param],
                                decoration: InputDecoration(
                                  labelText: param,
                                  hintText: '输入 $param 的值',
                                ),
                                onChanged: (_) => _updatePreview(template.content ?? ''),
                              ),
                            );
                          }).toList(),
                        ),
                      ],
                    ),
                  ),
                ),
              ],
              const SizedBox(height: 24),
              Card(
                child: Padding(
                  padding: const EdgeInsets.all(24),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const Row(
                        children: [
                          Icon(Icons.preview, color: AppColors.secondary),
                          SizedBox(width: 8),
                          Text(
                            '内容预览',
                            style: TextStyle(
                              fontSize: 18,
                              fontWeight: FontWeight.w600,
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(height: 16),
                      Container(
                        width: double.infinity,
                        padding: const EdgeInsets.all(16),
                        decoration: BoxDecoration(
                          color: AppColors.bgTertiary,
                          borderRadius: BorderRadius.circular(8),
                          border: Border.all(color: AppColors.border),
                        ),
                        child: SelectableText(
                          _previewContent,
                          style: const TextStyle(
                            fontSize: 14,
                            height: 1.6,
                          ),
                        ),
                      ),
                    ],
                  ),
                ),
              ),
              const SizedBox(height: 24),
              Card(
                child: Padding(
                  padding: const EdgeInsets.all(24),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const Row(
                        children: [
                          Icon(Icons.code, color: AppColors.warning),
                          SizedBox(width: 8),
                          Text(
                            '原始内容',
                            style: TextStyle(
                              fontSize: 18,
                              fontWeight: FontWeight.w600,
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(height: 16),
                      Container(
                        width: double.infinity,
                        padding: const EdgeInsets.all(16),
                        decoration: BoxDecoration(
                          color: AppColors.bgSecondary,
                          borderRadius: BorderRadius.circular(8),
                          border: Border.all(color: AppColors.border),
                        ),
                        child: SelectableText(
                          template.content ?? '',
                          style: TextStyle(
                            fontSize: 14,
                            height: 1.6,
                            fontFamily: 'monospace',
                            color: AppColors.textSecondary,
                          ),
                        ),
                      ),
                    ],
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildInfoRow(String label, String value) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 8),
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
}